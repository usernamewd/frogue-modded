package io.github.necrashter.natural_revenge.android;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import io.github.necrashter.natural_revenge.mods.CheatManager;

/**
 * ModMenuService - Android Floating Overlay Service for Mod Menu (Russian)
 */
public class ModMenuService extends Service {

    private WindowManager windowManager;
    private View floatingButton;
    private View menuPanel;
    private boolean isMenuOpen = false;

    private WindowManager.LayoutParams floatingButtonParams;
    private WindowManager.LayoutParams menuPanelParams;

    // Colors
    private static final int COLOR_PRIMARY = Color.parseColor("#1A1A2E");
    private static final int COLOR_SECONDARY = Color.parseColor("#16213E");
    private static final int COLOR_ACCENT = Color.parseColor("#E94560");
    private static final int COLOR_ACCENT_LIGHT = Color.parseColor("#FF6B6B");
    private static final int COLOR_SUCCESS = Color.parseColor("#4ADE80");
    private static final int COLOR_DANGER = Color.parseColor("#F43F5E");
    private static final int COLOR_TEXT = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT_DIM = Color.parseColor("#94A3B8");
    private static final int COLOR_AIMBOT = Color.parseColor("#8B5CF6");

    private CheatManager cheatManager;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        cheatManager = CheatManager.getInstance();

        createFloatingButton();
        createMenuPanel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingButton != null) {
            windowManager.removeView(floatingButton);
        }
        if (menuPanel != null && menuPanel.getParent() != null) {
            windowManager.removeView(menuPanel);
        }
    }

    private int getLayoutFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    private void createFloatingButton() {
        floatingButton = new FrameLayout(this);

        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setShape(GradientDrawable.OVAL);
        buttonBg.setColors(new int[]{COLOR_ACCENT, COLOR_ACCENT_LIGHT});
        buttonBg.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        TextView buttonText = new TextView(this);
        buttonText.setText("M");
        buttonText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        buttonText.setTextColor(COLOR_TEXT);
        buttonText.setTypeface(Typeface.DEFAULT_BOLD);
        buttonText.setGravity(Gravity.CENTER);

        FrameLayout buttonFrame = new FrameLayout(this);
        buttonFrame.setBackground(buttonBg);
        buttonFrame.addView(buttonText, new FrameLayout.LayoutParams(
                dpToPx(56), dpToPx(56), Gravity.CENTER));

        ((FrameLayout) floatingButton).addView(buttonFrame);

        floatingButtonParams = new WindowManager.LayoutParams(
                dpToPx(56),
                dpToPx(56),
                getLayoutFlag(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatingButtonParams.gravity = Gravity.TOP | Gravity.START;
        floatingButtonParams.x = 0;
        floatingButtonParams.y = dpToPx(100);

        floatingButton.setOnTouchListener(new FloatingButtonTouchListener());

        windowManager.addView(floatingButton, floatingButtonParams);
    }

    private void createMenuPanel() {
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setCornerRadius(dpToPx(16));
        panelBg.setColor(COLOR_PRIMARY);
        panelBg.setStroke(dpToPx(2), COLOR_ACCENT);
        mainContainer.setBackground(panelBg);
        mainContainer.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Header
        mainContainer.addView(createHeader());
        mainContainer.addView(createDivider());

        // Scrollable content
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(450)));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));

        // Add cheat sections (Russian)
        addCheatSection(contentLayout, "ИГРОК", createPlayerCheats());
        addCheatSection(contentLayout, "ОРУЖИЕ", createWeaponCheats());
        addCheatSection(contentLayout, "ДВИЖЕНИЕ", createMovementCheats());
        addCheatSection(contentLayout, "АИМБОТ", createAimbotCheats());
        addCheatSection(contentLayout, "МИР", createWorldCheats());
        addCheatSection(contentLayout, "ДЕЙСТВИЯ", createActionButtons());
        addCheatSection(contentLayout, "НАСТРОЙКИ", createSliders());

        scrollView.addView(contentLayout);
        mainContainer.addView(scrollView);

        mainContainer.addView(createDivider());
        mainContainer.addView(createFooter());

        menuPanel = mainContainer;

        menuPanelParams = new WindowManager.LayoutParams(
                dpToPx(340),
                WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutFlag(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        menuPanelParams.gravity = Gravity.CENTER;
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dpToPx(8));

        TextView title = new TextView(this);
        title.setText("FROGUE МОД МЕНЮ");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_ACCENT_LIGHT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(title);

        Button closeBtn = createStyledButton("X", COLOR_DANGER, dpToPx(36), dpToPx(36));
        closeBtn.setOnClickListener(v -> toggleMenu());
        header.addView(closeBtn);

        return header;
    }

    private View createDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(COLOR_SECONDARY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        params.setMargins(0, dpToPx(4), 0, dpToPx(4));
        divider.setLayoutParams(params);
        return divider;
    }

    private LinearLayout createFooter() {
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dpToPx(8), 0, 0);

        TextView credit = new TextView(this);
        credit.setText("Frogue Мод Меню v2.0");
        credit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        credit.setTextColor(COLOR_TEXT_DIM);
        footer.addView(credit);

        return footer;
    }

    private void addCheatSection(LinearLayout parent, String title, LinearLayout content) {
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sectionTitle.setTextColor(COLOR_ACCENT);
        sectionTitle.setTypeface(Typeface.DEFAULT_BOLD);
        sectionTitle.setPadding(0, dpToPx(12), 0, dpToPx(8));
        parent.addView(sectionTitle);

        parent.addView(content);
    }

    private LinearLayout createPlayerCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(createCheatToggle("Бессмертие", "Бесконечное здоровье",
                cheatManager.isGodMode(), enabled -> cheatManager.setGodMode(enabled)));

        layout.addView(createCheatToggle("Супер Прыжок", "Прыгать выше",
                cheatManager.isSuperJump(), enabled -> cheatManager.setSuperJump(enabled)));

        layout.addView(createCheatToggle("Ускорение", "Двигаться быстрее",
                cheatManager.isSpeedHack(), enabled -> cheatManager.setSpeedHack(enabled)));

        return layout;
    }

    private LinearLayout createWeaponCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(createCheatToggle("Бесконечные Патроны", "Патроны не кончаются",
                cheatManager.isInfiniteAmmo(), enabled -> cheatManager.setInfiniteAmmo(enabled)));

        layout.addView(createCheatToggle("Без Перезарядки", "Мгновенная перезарядка",
                cheatManager.isNoReload(), enabled -> cheatManager.setNoReload(enabled)));

        layout.addView(createCheatToggle("Быстрая Стрельба", "Увеличенная скорострельность",
                cheatManager.isRapidFire(), enabled -> cheatManager.setRapidFire(enabled)));

        layout.addView(createCheatToggle("Без Разброса", "Идеальная точность",
                cheatManager.isNoSpread(), enabled -> cheatManager.setNoSpread(enabled)));

        layout.addView(createCheatToggle("Без Отдачи", "Нет отдачи оружия",
                cheatManager.isNoRecoil(), enabled -> cheatManager.setNoRecoil(enabled)));

        layout.addView(createCheatToggle("Убийство с Одного Удара", "Огромный урон",
                cheatManager.isOneHitKill(), enabled -> cheatManager.setOneHitKill(enabled)));

        layout.addView(createCheatToggle("Много Слотов Оружия", "99 слотов для оружия",
                cheatManager.isUnlimitedWeaponSlots(), enabled -> cheatManager.setUnlimitedWeaponSlots(enabled)));

        return layout;
    }

    private LinearLayout createMovementCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(createCheatToggle("Банни Хоп", "Авто-прыжок при движении",
                cheatManager.isBunnyHop(), enabled -> cheatManager.setBunnyHop(enabled)));

        layout.addView(createCheatToggle("Воздушное Управление", "Лучший контроль в воздухе",
                cheatManager.isAirStrafe(), enabled -> cheatManager.setAirStrafe(enabled)));

        layout.addView(createCheatToggle("Третье Лицо", "Камера от третьего лица",
                cheatManager.isThirdPerson(), enabled -> cheatManager.setThirdPerson(enabled)));

        return layout;
    }

    private LinearLayout createAimbotCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Silent Aim toggle
        layout.addView(createCheatToggle("Тихий Аимбот", "Автоматическое наведение",
                cheatManager.isSilentAim(), enabled -> cheatManager.setSilentAim(enabled), COLOR_AIMBOT));

        // Draw FOV circle toggle
        layout.addView(createCheatToggle("Показать Зону Аима", "Отображать круг FOV",
                cheatManager.isDrawAimFov(), enabled -> cheatManager.setDrawAimFov(enabled), COLOR_AIMBOT));

        // Aim FOV slider
        layout.addView(createAimFovSlider());

        return layout;
    }

    private LinearLayout createAimFovSlider() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(8));
        bg.setColor(COLOR_SECONDARY);
        container.setBackground(bg);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, dpToPx(4), 0, dpToPx(4));
        container.setLayoutParams(containerParams);

        // Header row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView nameText = new TextView(this);
        nameText.setText("FOV Аимбота");
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameText.setTextColor(COLOR_TEXT);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(nameText);

        final TextView valueText = new TextView(this);
        valueText.setText((int) cheatManager.getAimFov() + "°");
        valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        valueText.setTextColor(COLOR_AIMBOT);
        headerRow.addView(valueText);

        container.addView(headerRow);

        // Slider (5-360)
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(355); // 360 - 5 = 355
        seekBar.setProgress((int) cheatManager.getAimFov() - 5);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fov = progress + 5;
                valueText.setText(fov + "°");
                cheatManager.setAimFov(fov);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        container.addView(seekBar);

        return container;
    }

    private LinearLayout createWorldCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(createCheatToggle("Заморозить Врагов", "Остановить всех врагов",
                cheatManager.isFreezeEnemies(), enabled -> cheatManager.setFreezeEnemies(enabled)));

        layout.addView(createCheatToggle("Дальность Обзора", "Видеть дальше",
                cheatManager.isExtendedViewDistance(), enabled -> cheatManager.setExtendedViewDistance(enabled)));

        return layout;
    }

    private LinearLayout createActionButtons() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Row 1
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER);

        Button killAllBtn = createStyledButton("Убить Всех", COLOR_DANGER, dpToPx(95), dpToPx(40));
        killAllBtn.setOnClickListener(v -> cheatManager.triggerInstantKillAll());
        row1.addView(killAllBtn);

        addHorizontalSpace(row1, dpToPx(8));

        Button healBtn = createStyledButton("Вылечить", COLOR_SUCCESS, dpToPx(95), dpToPx(40));
        healBtn.setOnClickListener(v -> cheatManager.healPlayer());
        row1.addView(healBtn);

        addHorizontalSpace(row1, dpToPx(8));

        Button weaponBtn = createStyledButton("Оружие", COLOR_ACCENT, dpToPx(95), dpToPx(40));
        weaponBtn.setOnClickListener(v -> cheatManager.giveAllWeapons());
        row1.addView(weaponBtn);

        layout.addView(row1);
        addVerticalSpace(layout, dpToPx(8));

        // Row 2
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER);

        Button teleportBtn = createStyledButton("Телепорт +20", COLOR_SECONDARY, dpToPx(140), dpToPx(40));
        teleportBtn.setOnClickListener(v -> cheatManager.teleportForward(20f));
        row2.addView(teleportBtn);

        addHorizontalSpace(row2, dpToPx(8));

        Button resetBtn = createStyledButton("Сбросить Всё", COLOR_SECONDARY, dpToPx(140), dpToPx(40));
        resetBtn.setOnClickListener(v -> {
            cheatManager.resetAllCheats();
            refreshMenuPanel();
        });
        row2.addView(resetBtn);

        layout.addView(row2);

        return layout;
    }

    private LinearLayout createSliders() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(createSliderControl("Множитель Скорости", 1, 10,
                (int) cheatManager.getSpeedMultiplier(),
                value -> cheatManager.setSpeedMultiplier(value)));

        layout.addView(createSliderControl("Множитель Прыжка", 1, 10,
                (int) cheatManager.getJumpMultiplier(),
                value -> cheatManager.setJumpMultiplier(value)));

        layout.addView(createSliderControl("Множитель Обзора", 1, 10,
                (int) cheatManager.getViewDistanceMultiplier(),
                value -> cheatManager.setViewDistanceMultiplier(value)));

        return layout;
    }

    // ==================== UI HELPERS ====================

    private LinearLayout createCheatToggle(String name, String description,
            boolean initialState, OnCheatToggleListener listener) {
        return createCheatToggle(name, description, initialState, listener, COLOR_ACCENT_LIGHT);
    }

    private LinearLayout createCheatToggle(String name, String description,
            boolean initialState, OnCheatToggleListener listener, int accentColor) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(8));
        bg.setColor(COLOR_SECONDARY);
        container.setBackground(bg);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, dpToPx(4), 0, dpToPx(4));
        container.setLayoutParams(containerParams);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        nameText.setTextColor(COLOR_TEXT);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        textContainer.addView(nameText);

        TextView descText = new TextView(this);
        descText.setText(description);
        descText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        descText.setTextColor(COLOR_TEXT_DIM);
        textContainer.addView(descText);

        container.addView(textContainer);

        CheckBox toggle = new CheckBox(this);
        toggle.setChecked(initialState);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onToggle(isChecked));
        container.addView(toggle);

        return container;
    }

    private LinearLayout createSliderControl(String name, int min, int max,
            int initialValue, OnSliderChangeListener listener) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(8));
        bg.setColor(COLOR_SECONDARY);
        container.setBackground(bg);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, dpToPx(4), 0, dpToPx(4));
        container.setLayoutParams(containerParams);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameText.setTextColor(COLOR_TEXT);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(nameText);

        final TextView valueText = new TextView(this);
        valueText.setText(initialValue + "x");
        valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        valueText.setTextColor(COLOR_ACCENT_LIGHT);
        headerRow.addView(valueText);

        container.addView(headerRow);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max - min);
        seekBar.setProgress(initialValue - min);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress + 1;
                valueText.setText(value + "x");
                listener.onValueChanged(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        container.addView(seekBar);

        return container;
    }

    private Button createStyledButton(String text, int bgColor, int width, int height) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(COLOR_TEXT);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        button.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(8));
        bg.setColor(bgColor);
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        button.setLayoutParams(params);
        button.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));

        return button;
    }

    private void addHorizontalSpace(LinearLayout parent, int width) {
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(width, 1));
        parent.addView(space);
    }

    private void addVerticalSpace(LinearLayout parent, int height) {
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height));
        parent.addView(space);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void toggleMenu() {
        if (isMenuOpen) {
            windowManager.removeView(menuPanel);
            isMenuOpen = false;
        } else {
            refreshMenuPanel();
            windowManager.addView(menuPanel, menuPanelParams);
            isMenuOpen = true;
        }
    }

    private void refreshMenuPanel() {
        if (menuPanel != null && menuPanel.getParent() != null) {
            windowManager.removeView(menuPanel);
        }
        createMenuPanel();
        if (isMenuOpen) {
            windowManager.addView(menuPanel, menuPanelParams);
        }
    }

    // ==================== TOUCH LISTENER ====================

    private class FloatingButtonTouchListener implements View.OnTouchListener {
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;
        private boolean isDragging = false;
        private long touchStartTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = floatingButtonParams.x;
                    initialY = floatingButtonParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    touchStartTime = System.currentTimeMillis();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) (event.getRawX() - initialTouchX);
                    int deltaY = (int) (event.getRawY() - initialTouchY);

                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isDragging = true;
                    }

                    if (isDragging) {
                        floatingButtonParams.x = initialX + deltaX;
                        floatingButtonParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(floatingButton, floatingButtonParams);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    long touchDuration = System.currentTimeMillis() - touchStartTime;
                    if (!isDragging && touchDuration < 200) {
                        toggleMenu();
                    }
                    return true;
            }
            return false;
        }
    }

    // ==================== INTERFACES ====================

    private interface OnCheatToggleListener {
        void onToggle(boolean enabled);
    }

    private interface OnSliderChangeListener {
        void onValueChanged(int value);
    }
}
