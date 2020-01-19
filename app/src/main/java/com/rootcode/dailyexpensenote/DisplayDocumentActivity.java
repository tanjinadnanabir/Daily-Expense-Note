package com.rootcode.dailyexpensenote;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayDocumentActivity extends AppCompatActivity {

    private TextView documentTitleTV;
    private ImageView expenseDocumentIV;
    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_document);

        this.setTitle("Document");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        documentTitleTV = findViewById(R.id.documentTitleTV);
        expenseDocumentIV = findViewById(R.id.expenseDocumentIV);
        helper = new DatabaseHelper(this);

        //showing document operation
        String getId = getIntent().getStringExtra("Id");
        int id = Integer.valueOf(getId);

        Cursor cursor = helper.getDocument(id);
        while (cursor.moveToNext()) {
            String documentUrl = cursor.getString(cursor.getColumnIndex(helper.COL_Document));
            if (documentUrl.equals("null") || documentUrl.equals("")) {
                expenseDocumentIV.setVisibility(View.GONE);
                documentTitleTV.setText("Not found any document");
                documentTitleTV.setTextColor(Color.RED);
            } else {
                documentTitleTV.setTextColor(Color.WHITE);
                Bitmap bitmap = decodeBase64(documentUrl);
                expenseDocumentIV.setVisibility(View.VISIBLE);
                expenseDocumentIV.setImageBitmap(bitmap);
            }
        }
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
