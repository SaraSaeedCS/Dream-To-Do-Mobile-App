package com.example.todointerfaces;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap; // Added for multi-page logic
import android.graphics.Canvas; // Added for multi-page logic
import android.graphics.Color;
import android.graphics.Paint; // Added for multi-page logic
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager; // Restored
import android.os.storage.StorageVolume; // Restored
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // Added for user feedback

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todointerfaces.Adapter.tdDoAdapter;
import com.example.todointerfaces.Model.toDoModel;
import com.example.todointerfaces.Utils.DatabaseHandler;
import com.example.todointerfaces.Utils.ThemeManager;
import com.example.todointerfaces.Utils.UserSessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HomePage extends AppCompatActivity implements DialogCloseListener {

    private TextView quoteText;
    private List<String> quotes;

    private RecyclerView recyclerView;
    private tdDoAdapter taskAdapter;
    private List<toDoModel> taskList;
    private List<toDoModel> allTasks;

    private DatabaseHandler db;
    private FloatingActionButton fab;
    private Spinner spinnerFilterCategory;
    private String currentFilter = "All";

    private UserSessionManager session;
    private int currentUserId = -1;

    private Toolbar toolbar;
    private File pdfFile;

    private static final int lavendr = 0xFFb7b5dd;
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DatabaseHandler(this);
        db.openDataBase();
        session = new UserSessionManager(this);
        currentUserId = session.getUserId();
        if (currentUserId == -1) {
            Intent intent = new Intent(HomePage.this, MainPage.class);
            startActivity(intent);
            finish();
            return;
        }


        recyclerView = findViewById(R.id.taskRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        quoteText = findViewById(R.id.quteViwe);
        fab = findViewById(R.id.floatingActionButton2);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        taskAdapter = new tdDoAdapter(db, this);
        recyclerView.setAdapter(taskAdapter);
        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new RecyclerItemTouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Category filter spinner
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_categories_filter,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(filterAdapter);

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //if nothing selected
            }
        });

        loadTasksFromDb();
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, AddingTask.class);
            startActivity(intent);
        });
        quotes = loadQuotes(this);
        quoteText.setText(getRandomQuote(quotes));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(HomePage.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_save) {
            // Old devices API 28 and below
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                    return true;
                }
            }
            // New devices API 29/Q and above
            try {
                savetoStorge();
            } catch (IOException e) {
                Toast.makeText(this, "Save Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void savetoStorge() throws IOException {
        Bitmap bigBitmap = getBitmapFromRecyclerView(recyclerView);
        if (bigBitmap == null) {
            Toast.makeText(this, "No tasks to save.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfUsingMediaStore(bigBitmap);
        } else {
            savePdfUsingLegacyMethod(bigBitmap);
        }
    }
    // save PDF Android 10 and above
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void savePdfUsingMediaStore(Bitmap bigBitmap) {
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "tasks" + System.currentTimeMillis() + ".pdf");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        Uri pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        if (pdfUri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                PdfDocument document = new PdfDocument();
                int contentWidth = recyclerView.getWidth();
                int contentHeight = recyclerView.getHeight();
                int totalBitmapHeight = bigBitmap.getHeight();
                int currentPageTop = 0;
                while (currentPageTop < totalBitmapHeight) {
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                            contentWidth, contentHeight, document.getPages().size() + 1
                    ).create();
                    PdfDocument.Page page = document.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    int remainingHeight = totalBitmapHeight - currentPageTop;
                    int drawHeight = Math.min(contentHeight, remainingHeight);
                    Bitmap bmpPage = Bitmap.createBitmap(bigBitmap, 0, currentPageTop, contentWidth, drawHeight);
                    canvas.drawBitmap(bmpPage, 0, 0, null);
                    document.finishPage(page);
                    currentPageTop += contentHeight;
                    bmpPage.recycle();
                }
                document.writeTo(outputStream);
                Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                bigBitmap.recycle();
            }
        }
    }

    // save PDF Android 9 and below
    private void savePdfUsingLegacyMethod(Bitmap bigBitmap) throws IOException {
        File directory = null;
        try {
            StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
            StorageVolume storageVolume = storageManager.getStorageVolumes().get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && storageVolume.getDirectory() != null) {
                directory = storageVolume.getDirectory();
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                directory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            }
        } catch (Exception e) {
            directory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
        }
        if (directory == null) {
            Toast.makeText(this, "Cannot determine storage directory.", Toast.LENGTH_LONG).show();
            return;
        }
        File downloadsDir = new File(directory, "Download");
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            downloadsDir = directory;
        }
        pdfFile = new File(downloadsDir, "tasks" + String.valueOf(System.currentTimeMillis()) + ".pdf");

        final int MARGIN = 50;
        PdfDocument document = new PdfDocument();
        int contentWidth = recyclerView.getWidth();
        int contentHeight = recyclerView.getHeight();
        // Page size includes margins
        int pageWidth = contentWidth + 2 * MARGIN;
        int pageHeight = contentHeight + 2 * MARGIN;
        int totalBitmapHeight = bigBitmap.getHeight();
        int currentPageTop = 0;
        while (currentPageTop < totalBitmapHeight) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, document.getPages().size() + 1
            ).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            int remainingHeight = totalBitmapHeight - currentPageTop;
            int drawHeight = Math.min(contentHeight, remainingHeight);
            Bitmap bmpPage = Bitmap.createBitmap(bigBitmap, 0, currentPageTop, contentWidth, drawHeight);
            canvas.drawBitmap(bmpPage, MARGIN, MARGIN, null);
            document.finishPage(page);
            currentPageTop += contentHeight;
            bmpPage.recycle();
        }

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
            Toast.makeText(this, "PDF saved successfully to: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
            bigBitmap.recycle();
        }
    }
    // large Bitmap = recycaler view content
    private Bitmap getBitmapFromRecyclerView(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return null;
        Paint paint = new Paint();
        int size = adapter.getItemCount();
        int height = 0;
        final List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            RecyclerView.ViewHolder holder = adapter.createViewHolder(
                    (android.view.ViewGroup) recyclerView.getParent(),
                    adapter.getItemViewType(i)
            );
            adapter.onBindViewHolder(holder, i);


            holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            int measuredWidth = holder.itemView.getMeasuredWidth();
            int measuredHeight = holder.itemView.getMeasuredHeight();
            holder.itemView.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth,
                    measuredHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(lavendr);
            holder.itemView.draw(canvas);
            bitmaps.add(bitmap);
            height += measuredHeight;
        }
        Bitmap bigBitmap = Bitmap.createBitmap(recyclerView.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
        Canvas bigCanvas = new Canvas(bigBitmap);
        int top = 0;
        for (Bitmap bmp : bitmaps) {
            bigCanvas.drawBitmap(bmp, 0f, top, paint);
            top += bmp.getHeight();
            bmp.recycle();
        }

        return bigBitmap;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    savetoStorge();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "Storage permission is required to save the PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // DialogCloseListener
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        loadTasksFromDb();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (quotes != null && !quotes.isEmpty()) {
            quoteText.setText(getRandomQuote(quotes));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDb();
    }
    private void loadTasksFromDb() {
        if (currentUserId > 0) {
            allTasks = db.getTasksForUser(currentUserId);
        } else {
            allTasks = db.getAllTasks();
        }
        Collections.reverse(allTasks);
        applyFilter();
    }
    private void applyFilter() {
        if (allTasks == null) return;

        List<toDoModel> filtered = new ArrayList<>();
        if ("All".equalsIgnoreCase(currentFilter)) {
            filtered.addAll(allTasks);
        } else {
            for (toDoModel t : allTasks) {
                if (t.getCategory() != null &&
                        currentFilter.equalsIgnoreCase(t.getCategory())) {
                    filtered.add(t);
                }
            }
        }
        taskList = filtered;
        taskAdapter.setTasks(taskList);
        taskAdapter.notifyDataSetChanged();
    }
    public List<String> loadQuotes(Context context) {
        List<String> quotesList = new ArrayList<>();
        InputStream inputStream = context.getResources().openRawResource(R.raw.quotes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Author")) continue;

                String[] columns = line.split("\",\"");
                if (columns.length > 1) {
                    String quote = columns[1].replace("\"", "");
                    quotesList.add(quote);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return quotesList;
    }

    public String getRandomQuote(List<String> quotes) {
        if (quotes == null || quotes.isEmpty()) return "";
        Random random = new Random();
        return quotes.get(random.nextInt(quotes.size()));
    }
}