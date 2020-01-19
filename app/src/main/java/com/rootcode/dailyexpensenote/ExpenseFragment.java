package com.rootcode.dailyexpensenote;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpenseFragment extends Fragment {

    private Spinner typeSpinner;
    private TextView fromDateTV, toDateTV;
    private ImageView fromDateBtn, toDateBtn;

    String[] typeExpense;
    private String typeOfExpense;
    private int count = 0;

    private FloatingActionButton addFloatAB;
    private RecyclerView expenseRV;

    private List<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;
    private DatabaseHelper helper;
    private Context context;

    private Calendar calendar;
    private int year, month, fromDay, toDay, hour, minute;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public ExpenseFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        context = container.getContext();
        init(view);
        addData();
        Log.d("prob", "init");
        try {
            initFiltardata(view);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        initR();
        try {
            getdata();
            Log.d("prob", "getdata");
        } catch (ParseException e) {
            Log.d("prob", "get data prob 1");
            e.printStackTrace();
        }
        return view;
    }

    private void initFiltardata(final View view) throws ParseException {
        typeExpense = new String[]{"All", "Rent", "Food", "Utility bills", "Medicine", "Clothing", "Transport", "Health", "Gift"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_activated_1, typeExpense);
        typeSpinner.setAdapter(arrayAdapter);
        typeOfExpense = typeExpense[0];

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                typeOfExpense = typeExpense[position];
                if (count > 0) {                     //set listener to pull modyifiying data when second time data set
                    try {
                        getdata();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        int indexMonth = month + 1;
        fromDay = 1;
        toDay = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);

        String nFromDate = year + "/" + indexMonth + "/" + fromDay;
        fromDateTV.setText(nFromDate);

        final DatePickerDialog.OnDateSetListener fromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int fromDay) {
                calendar.set(year, month, fromDay);
                String userFromDate = dateFormat.format(calendar.getTime());
                // Toast.makeText(context,userFromDate+" is selected",Toast.LENGTH_LONG).show();
                fromDateTV.setText(userFromDate);

                if (count > 0) {
                    try {
                        getdata();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        fromDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), fromDateListener, year, month, fromDay);
                datePickerDialog.show();
            }
        });

        String nToDate = year + "/" + indexMonth + "/" + toDay;
        toDateTV.setText(nToDate);

        final DatePickerDialog.OnDateSetListener toDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int toDay) {
                calendar.set(year, month, toDay);
                String userFromDate = dateFormat.format(calendar.getTime());
                toDateTV.setText(userFromDate);

                if (count > 0) {
                    try {
                        getdata();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        toDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), toDateListener, year, month, toDay);
                datePickerDialog.show();
            }
        });
    }

    private void addData() {
        addFloatAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddExpenseFragment addExpenseFragment = new AddExpenseFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.frameLayoutID, addExpenseFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    private void getdata() throws ParseException {

        Date d1 = dateFormat.parse(fromDateTV.getText().toString());
        Date d2 = dateFormat.parse(toDateTV.getText().toString());
        long fromDate = d1.getTime();
        long toDate = d2.getTime();

        expenseList.clear();
        expenseAdapter.notifyDataSetChanged();

        Cursor cursor = helper.showAllData();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(helper.COL_ID));
            String timeTo = cursor.getString(cursor.getColumnIndex(helper.COL_TIME));
            int amount = cursor.getInt(cursor.getColumnIndex(helper.COL_Amount));
            String type = cursor.getString(cursor.getColumnIndex(helper.COL_TYPE));

            long dateFromDB =Long.parseLong(cursor.getString(cursor.getColumnIndex(helper.COL_Date)));
            String dateTo = dateFormat.format(dateFromDB);
            count++;

            // Toast.makeText(context,"Check date : "+dateFromDB,Toast.LENGTH_LONG).show();

            if (typeOfExpense.equals(typeExpense[0]) && dateFromDB >= fromDate && dateFromDB <= toDate) {
                expenseList.add(new Expense(amount, id, type,dateTo,timeTo, null));
                expenseAdapter.notifyDataSetChanged();
            } else if (typeOfExpense.equals(type) && dateFromDB >= fromDate && dateFromDB <= toDate) {
                expenseList.add(new Expense(amount, id, type,dateTo,timeTo, null));
                expenseAdapter.notifyDataSetChanged();
            } else {
                // expenseList.clear();
                //adapterExpense.notifyDataSetChanged();
            }

            Log.d("prob", "id="+id+"  timeTo="+timeTo+"  amount="+amount+"  type="+type+"  dateFromDB="+dateFromDB);
        }
    }

    private void initR() {
        expenseRV.setLayoutManager(new LinearLayoutManager(context));
        expenseRV.setAdapter(expenseAdapter);
    }

    private void init(View view) {
        helper = new DatabaseHelper(context);
        addFloatAB = view.findViewById(R.id.addFloatAB);
        expenseRV = view.findViewById(R.id.expenseRV);
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(context, expenseList, helper);
        fromDateBtn = view.findViewById(R.id.fromDateCalenderBtn);
        toDateBtn = view.findViewById(R.id.toDateCalenderBtn);
        typeSpinner = view.findViewById(R.id.expenseTypeSpinner);
        fromDateTV = view.findViewById(R.id.fromDateTV);
        toDateTV = view.findViewById(R.id.toDateTV);
    }
}
