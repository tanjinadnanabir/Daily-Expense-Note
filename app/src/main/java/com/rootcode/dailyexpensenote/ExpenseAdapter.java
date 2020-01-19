package com.rootcode.dailyexpensenote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private Context context;
    private List<Expense> expenseList;
    private DatabaseHelper helper;
    private View getView;

    public ExpenseAdapter() {

    }

    public ExpenseAdapter(Context context, List<Expense> expenseList, DatabaseHelper helper) {
        this.context = context;
        this.expenseList = expenseList;
        this.helper = helper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_model_design, parent, false);
        getView = view;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Expense currentExpense = expenseList.get(position);
        holder.expenseType.setText(currentExpense.getType());
        holder.date.setText(currentExpense.getDate());
        holder.amount.setText("" + currentExpense.getAmount());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                View detailssheet = LayoutInflater.from(context).inflate(R.layout.details_expense_sheet, null);
//
//                BottomSheetDialog sheetDialog = new BottomSheetDialog(context);
//                sheetDialog.setContentView(detailssheet);
//                sheetDialog.show();

                DetailsExpenseSheet detailsExpenseSheet = new DetailsExpenseSheet(currentExpense.getId(), currentExpense.getDate(), currentExpense.getType(), currentExpense.getTime(), currentExpense.getAmount());
                detailsExpenseSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "Expense Details");
            }
        });

        holder.popupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu menu = new PopupMenu(context, v);
                menu.getMenuInflater().inflate(R.menu.update_delete, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.deleteItem:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Are you sure to delete ?");
                                builder.setCancelable(false);
                                builder.setIcon(R.drawable.ic_delete_forever_black_24dp);

                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        helper = new DatabaseHelper(context);
                                        helper.deleteData(currentExpense.getId());
                                        expenseList.remove(position);
                                        notifyDataSetChanged();
                                        dialog.cancel();
                                    }
                                });

                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.create();
                                builder.show();
                                break;

                            case R.id.updateItem:
                                requestUpdate(currentExpense);
                                break;

                            default:
                        }
                        return false;
                    }
                });
                menu.show();
            }
        });
    }

    private void requestUpdate(Expense currentExpense) {

        UpdateExpenseFragment updateExpenseFragment = new UpdateExpenseFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", currentExpense.getType());
        bundle.putString("id", String.valueOf(currentExpense.getId()));
        bundle.putString("date", currentExpense.getDate());
        bundle.putString("time", currentExpense.getTime());
        bundle.putString("amount", String.valueOf(currentExpense.getAmount()));
        updateExpenseFragment.setArguments(bundle);

        AppCompatActivity activity = (AppCompatActivity) getView.getContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutID, updateExpenseFragment).addToBackStack(null).commit();
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView expenseType, amount, date;
        ImageView popupBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            expenseType = itemView.findViewById(R.id.expenseTypeTV);
            date = itemView.findViewById(R.id.expenseDateTV);
            amount = itemView.findViewById(R.id.expenseAmountTV);
            popupBtn = itemView.findViewById(R.id.popupMenuBtn);
        }
    }
}
