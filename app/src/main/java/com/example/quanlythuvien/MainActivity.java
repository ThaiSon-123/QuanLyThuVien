package com.example.quanlythuvien;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.CauHinhDao;
import com.example.quanlythuvien.db.StatDao;
import com.example.quanlythuvien.db.UserDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String ROLE_ADMIN = "admin";

    private TextView tvBorrowCount;
    private TextView tvReturnCount;
    private StatDao statDao;
    private CauHinhDao cauHinhDao;
    private String currentRole;

    private android.widget.LinearLayout alertsGroup;
    private android.widget.LinearLayout alertOverdue;
    private android.widget.LinearLayout alertNearDue;
    private android.widget.LinearLayout alertLowStock;
    private TextView tvAlertOverdue;
    private TextView tvAlertNearDue;
    private TextView tvAlertNearDueSub;
    private TextView tvAlertLowStock;
    private TextView tvAlertLowStockSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statDao = new StatDao(this);
        cauHinhDao = CauHinhDao.getInstance(this);
        currentRole = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_ROLE, "");

        tvBorrowCount = findViewById(R.id.tvBorrowCount);
        tvReturnCount = findViewById(R.id.tvReturnCount);

        alertsGroup = findViewById(R.id.alertsGroup);
        alertOverdue = findViewById(R.id.alertOverdue);
        alertNearDue = findViewById(R.id.alertNearDue);
        alertLowStock = findViewById(R.id.alertLowStock);
        tvAlertOverdue = findViewById(R.id.tvAlertOverdue);
        tvAlertNearDue = findViewById(R.id.tvAlertNearDue);
        tvAlertNearDueSub = findViewById(R.id.tvAlertNearDueSub);
        tvAlertLowStock = findViewById(R.id.tvAlertLowStock);
        tvAlertLowStockSub = findViewById(R.id.tvAlertLowStockSub);

        setupTopBar();
        setupShortcuts();
        setupAlerts();
        setupBottomNav();
    }

    private void setupAlerts() {
        alertOverdue.setOnClickListener(v -> {
            Intent i = new Intent(this, MuonTraActivity.class);
            i.putExtra(MuonTraActivity.EXTRA_FILTER, MuonTraActivity.FILTER_OVERDUE);
            startActivity(i);
        });
        alertNearDue.setOnClickListener(v -> {
            Intent i = new Intent(this, MuonTraActivity.class);
            i.putExtra(MuonTraActivity.EXTRA_FILTER, MuonTraActivity.FILTER_NEAR_DUE);
            startActivity(i);
        });
        alertLowStock.setOnClickListener(v -> {
            Intent i = new Intent(this, SachActivity.class);
            i.putExtra(SachActivity.EXTRA_FILTER, SachActivity.FILTER_LOW_STOCK);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
    }

    private void refreshStats() {
        tvBorrowCount.setText(String.valueOf(statDao.countBorrow()));
        tvReturnCount.setText(String.valueOf(statDao.countReturn()));
        refreshAlerts();
    }

    private void refreshAlerts() {
        cauHinhDao.invalidate();
        int nearDays = cauHinhDao.nearDueDays();
        int lowStock = cauHinhDao.lowStockThreshold();

        int overdue = statDao.countOverdue();
        int nearDue = statDao.countNearDue(nearDays);
        int low     = statDao.countLowStock(lowStock);

        alertOverdue.setVisibility(overdue > 0 ? View.VISIBLE : View.GONE);
        alertNearDue.setVisibility(nearDue > 0 ? View.VISIBLE : View.GONE);
        alertLowStock.setVisibility(low > 0 ? View.VISIBLE : View.GONE);

        tvAlertOverdue.setText(overdue + " phiếu QUÁ HẠN");
        tvAlertNearDue.setText(nearDue + " phiếu sắp hết hạn");
        tvAlertNearDueSub.setText("Trong " + nearDays + " ngày tới");
        tvAlertLowStock.setText(low + " sách sắp hết kho");
        tvAlertLowStockSub.setText("Còn ≤ " + lowStock + " quyển");

        alertsGroup.setVisibility(
                (overdue + nearDue + low) > 0 ? View.VISIBLE : View.GONE);
    }

    private void setupTopBar() {
        ImageView btnMenu = findViewById(R.id.btnMenu);
        ImageView btnAccount = findViewById(R.id.btnAccount);
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvUserName = findViewById(R.id.tvUserName);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String hello;
        if (hour < 12) hello = "Chào buổi sáng 👋";
        else if (hour < 18) hello = "Chào buổi chiều 👋";
        else hello = "Chào buổi tối 👋";
        tvGreeting.setText(hello);


        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(LoginActivity.KEY_USERNAME, "");
        String rawName = new UserDao(this).findStaffName(username);
        String displayName = (rawName == null || rawName.isEmpty()) ? username : rawName;
        tvUserName.setText(displayName);

        btnMenu.setOnClickListener(v -> showMenuPopup(btnMenu));
        btnAccount.setOnClickListener(v -> showAccountDialog(prefs, username, displayName));
    }

    private void showMenuPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_main_popup, popup.getMenu());

        try {
            java.lang.reflect.Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            if (menuPopupHelper != null) {
                java.lang.reflect.Method method =
                        menuPopupHelper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
                method.setAccessible(true);
                method.invoke(menuPopupHelper, true);
            }
        } catch (Exception ignored) { }

        popup.getMenu().findItem(R.id.menu_staff)
                .setVisible(ROLE_ADMIN.equals(currentRole));
        popup.getMenu().findItem(R.id.menu_settings)
                .setVisible(ROLE_ADMIN.equals(currentRole));

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_book)     { openBook();     return true; }
            if (id == R.id.menu_borrow)   { openBorrow();   return true; }
            if (id == R.id.menu_reader)   { openReader();   return true; }
            if (id == R.id.menu_report)   { openReport();   return true; }
            if (id == R.id.menu_contact)  { openContact();  return true; }
            if (id == R.id.menu_staff)    { openStaff();    return true; }
            if (id == R.id.menu_settings) { openSettings(); return true; }
            if (id == R.id.menu_logout)   { confirmLogout(); return true; }
            return false;
        });
        popup.show();
    }

    private void showAccountDialog(SharedPreferences prefs, String username, String displayName) {
        String role = prefs.getString(LoginActivity.KEY_ROLE, "");
        String roleLabel = ROLE_ADMIN.equals(role) ? "Quản trị viên" : "Nhân viên";

        new AlertDialog.Builder(this)
                .setTitle("Tài khoản")
                .setMessage("👤  " + displayName + "\n\n"
                        + "Tên đăng nhập: " + username + "\n"
                        + "Vai trò: " + roleLabel)
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Đăng xuất", (d, w) -> confirmLogout())
                .show();
    }

    private void setupShortcuts() {
        findViewById(R.id.statBorrow).setOnClickListener(v -> openBorrow());
        findViewById(R.id.statReturn).setOnClickListener(v -> openBorrow());

        findViewById(R.id.favBook).setOnClickListener(v -> openBook());
        findViewById(R.id.favReader).setOnClickListener(v -> openReader());
        findViewById(R.id.favContact).setOnClickListener(v -> openContact());
        findViewById(R.id.favBorrow).setOnClickListener(v -> openBorrow());
        findViewById(R.id.favReport).setOnClickListener(v -> openReport());
        findViewById(R.id.favStaff).setOnClickListener(v -> openStaff());

        findViewById(R.id.recentBook).setOnClickListener(v -> openBook());
        findViewById(R.id.recentReader).setOnClickListener(v -> openReader());
        findViewById(R.id.recentStaff).setOnClickListener(v -> openStaff());

        boolean isAdmin = ROLE_ADMIN.equals(currentRole);
        findViewById(R.id.favStaff).setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.recentStaff).setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_book) {
                    openBook();
                    return false;
                } else if (id == R.id.nav_borrow) {
                    openBorrow();
                    return false;
                } else if (id == R.id.nav_reader) {
                    openReader();
                    return false;
                } else if (id == R.id.nav_logout) {
                    confirmLogout();
                    return false;
                }
                return false;
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (d, w) -> doLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doLogout() {
        getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openBook() {
        startActivity(new Intent(this, SachActivity.class));
    }
    private void openReader() {
        startActivity(new Intent(this, BanDocActivity.class));
    }
    private void openContact() {
        startActivity(new Intent(this, LienHeActivity.class));
    }
    private void openBorrow() {
        startActivity(new Intent(this, MuonTraActivity.class));
    }
    private void openReport() {
        startActivity(new Intent(this, BaoCaoActivity.class));
    }
    private void openStaff() {
        if (!ROLE_ADMIN.equals(currentRole)) {
            toast("Bạn không có quyền truy cập");
            return;
        }
        startActivity(new Intent(this, NhanVienActivity.class));
    }

    private void openSettings() {
        if (!ROLE_ADMIN.equals(currentRole)) {
            toast("Bạn không có quyền truy cập");
            return;
        }
        startActivity(new Intent(this, CauHinhActivity.class));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
