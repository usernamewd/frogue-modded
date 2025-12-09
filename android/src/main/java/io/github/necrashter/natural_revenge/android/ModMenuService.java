package io.github.necrashter.natural_revenge.android;

import android.app.Service;
import android.content.Context;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import io.github.necrashter.natural_revenge.mods.CheatManager;

/**
 * ModMenuService - Android Floating Overlay Service for Mod Menu
 * Provides a draggable floating button that opens a cheat menu overlay
 */
public class ModMenuService extends Service {

    private WindowManager windowManager;
    private View floatingButton;
    private View menuPanel;
    private boolean isMenuOpen = false;

    // Layout parameters
    private WindowManager.LayoutParams floatingButtonParams;
    private WindowManager.LayoutParams menuPanelParams;

    // Colors
    private static final int COLOR_PRIMARY = Color.parseColor("#1E1E2E");
    private static final int COLOR_SECONDARY = Color.parseColor("#2D2D44");
    private static final int COLOR_ACCENT = Color.parseColor("#7C3AED");
    private static final int COLOR_ACCENT_LIGHT = Color.parseColor("#A78BFA");
    private static final int COLOR_SUCCESS = Color.parseColor("#10B981");
    private static final int COLOR_DANGER = Color.parseColor("#EF4444");
    private static final int COLOR_TEXT = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT_DIM = Color.parseColor("#A0A0A0");

    // Cheat manager reference
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

    /**
     * Create the floating action button
     */
    private void createFloatingButton() {
        floatingButton = new FrameLayout(this);

        // Create button with gradient background
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

        // Window layout params
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

        // Touch listener for drag and click
        floatingButton.setOnTouchListener(new FloatingButtonTouchListener());

        windowManager.addView(floatingButton, floatingButtonParams);
    }

    /**
     * Create the main menu panel
     */
    private void createMenuPanel() {
        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setCornerRadius(dpToPx(16));
        panelBg.setColor(COLOR_PRIMARY);
        panelBg.setStroke(dpToPx(2), COLOR_ACCENT);
        mainContainer.setBackground(panelBg);
        mainContainer.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Header
        LinearLayout header = createHeader();
        mainContainer.addView(header);

        // Divider
        mainContainer.addView(createDivider());

        // Scrollable content
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(400)));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));

        // Add cheat sections
        addCheatSection(contentLayout, "PLAYER CHEATS", createPlayerCheats());
        addCheatSection(contentLayout, "WEAPON CHEATS", createWeaponCheats());
        addCheatSection(contentLayout, "WORLD CHEATS", createWorldCheats());
        addCheatSection(contentLayout, "ACTIONS", createActionButtons());
        addCheatSection(contentLayout, "SLIDERS", createSliders());

        scrollView.addView(contentLayout);
        mainContainer.addView(scrollView);

        // Footer
        mainContainer.addView(createDivider());
        mainContainer.addView(createFooter());

        menuPanel = mainContainer;

        // Window layout params
        menuPanelParams = new WindowManager.LayoutParams(
                dpToPx(320),
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

        // Title
        TextView title = new TextView(this);
        title.setText("FROGUE MOD MENU");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_ACCENT_LIGHT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(title);

        // Close button
        Button closeBtn = createStyledButton("X", COLOR_DANGER, dpToPx(36), dpToPx(36));
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });
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
        credit.setText("Frogue Mod Menu v1.0");
        credit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        credit.setTextColor(COLOR_TEXT_DIM);
        footer.addView(credit);

        return footer;
    }

    private void addCheatSection(LinearLayout parent, String title, LinearLayout content) {
        // Section header
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sectionTitle.setTextColor(COLOR_ACCENT);
        sectionTitle.setTypeface(Typeface.DEFAULT_BOLD);
        sectionTitle.setPadding(0, dpToPx(12), 0, dpToPx(8));
        parent.addView(sectionTitle);

        // Content
        parent.addView(content);
    }

    /**
     * Player cheats section
     */
    private LinearLayout createPlayerCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // God Mode
        layout.addView(createCheatToggle("God Mode", "Invincibility",
                cheatManager.isGodMode(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setGodMode(enabled);
            }
        }));

        // Speed Hack
        layout.addView(createCheatToggle("Speed Hack", "Move faster (2x)",
                cheatManager.isSpeedHack(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setSpeedHack(enabled);
            }
        }));

        // Super Jump
        layout.addView(createCheatToggle("Super Jump", "Jump higher (2x)",
                cheatManager.isSuperJump(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setSuperJump(enabled);
            }
        }));

        return layout;
    }

    /**
     * Weapon cheats section
     */
    private LinearLayout createWeaponCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Infinite Ammo
        layout.addView(createCheatToggle("Infinite Ammo", "Never run out",
                cheatManager.isInfiniteAmmo(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setInfiniteAmmo(enabled);
            }
        }));

        // No Reload
        layout.addView(createCheatToggle("No Reload", "Skip reload time",
                cheatManager.isNoReload(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setNoReload(enabled);
            }
        }));

        // Rapid Fire
        layout.addView(createCheatToggle("Rapid Fire", "Shoot faster",
                cheatManager.isRapidFire(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setRapidFire(enabled);
            }
        }));

        // No Spread
        layout.addView(createCheatToggle("No Spread", "Perfect accuracy",
                cheatManager.isNoSpread(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setNoSpread(enabled);
            }
        }));

        // One Hit Kill
        layout.addView(createCheatToggle("One Hit Kill", "Massive damage",
                cheatManager.isOneHitKill(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setOneHitKill(enabled);
            }
        }));

        // Unlimited Weapon Slots
        layout.addView(createCheatToggle("Unlimited Slots", "99 weapon slots",
                cheatManager.isUnlimitedWeaponSlots(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setUnlimitedWeaponSlots(enabled);
            }
        }));

        return layout;
    }

    /**
     * World cheats section
     */
    private LinearLayout createWorldCheats() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Freeze Enemies
        layout.addView(createCheatToggle("Freeze Enemies", "Stop all enemies",
                cheatManager.isFreezeEnemies(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setFreezeEnemies(enabled);
            }
        }));

        // Extended View Distance
        layout.addView(createCheatToggle("Extended View", "See further (3x)",
                cheatManager.isExtendedViewDistance(), new OnCheatToggleListener() {
            @Override
            public void onToggle(boolean enabled) {
                cheatManager.setExtendedViewDistance(enabled);
            }
        }));

        return layout;
    }

    /**
     * Action buttons section
     */
    private LinearLayout createActionButtons() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Button row 1
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER);

        Button killAllBtn = createStyledButton("Kill All", COLOR_DANGER, dpToPx(90), dpToPx(40));
        killAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheatManager.triggerInstantKillAll();
            }
        });
        row1.addView(killAllBtn);

        addHorizontalSpace(row1, dpToPx(8));

        Button healBtn = createStyledButton("Full Heal", COLOR_SUCCESS, dpToPx(90), dpToPx(40));
        healBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheatManager.healPlayer();
            }
        });
        row1.addView(healBtn);

        addHorizontalSpace(row1, dpToPx(8));

        Button weaponBtn = createStyledButton("Get Gun", COLOR_ACCENT, dpToPx(90), dpToPx(40));
        weaponBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheatManager.giveAllWeapons();
            }
        });
        row1.addView(weaponBtn);

        layout.addView(row1);
        addVerticalSpace(layout, dpToPx(8));

        // Button row 2
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER);

        Button teleportBtn = createStyledButton("Teleport +20", COLOR_SECONDARY, dpToPx(120), dpToPx(40));
        teleportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheatManager.teleportForward(20f);
            }
        });
        row2.addView(teleportBtn);

        addHorizontalSpace(row2, dpToPx(8));

        Button resetBtn = createStyledButton("Reset All", COLOR_SECONDARY, dpToPx(120), dpToPx(40));
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheatManager.resetAllCheats();
                refreshMenuPanel();
            }
        });
        row2.addView(resetBtn);

        layout.addView(row2);

        return layout;
    }

    /**
     * Slider controls section
     */
    private LinearLayout createSliders() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Speed multiplier slider
        layout.addView(createSliderControl("Speed Multiplier", 1, 10,
                (int) cheatManager.getSpeedMultiplier(), new OnSliderChangeListener() {
            @Override
            public void onValueChanged(int value) {
                cheatManager.setSpeedMultiplier(value);
            }
        }));

        // Jump multiplier slider
        layout.addView(createSliderControl("Jump Multiplier", 1, 10,
                (int) cheatManager.getJumpMultiplier(), new OnSliderChangeListener() {
            @Override
            public void onValueChanged(int value) {
                cheatManager.setJumpMultiplier(value);
            }
        }));

        // View distance slider
        layout.addView(createSliderControl("View Distance", 1, 10,
                (int) cheatManager.getViewDistanceMultiplier(), new OnSliderChangeListener() {
            @Override
            public void onValueChanged(int value) {
                cheatManager.setViewDistanceMultiplier(value);
            }
        }));

        return layout;
    }

    // ==================== UI HELPERS ====================

    private LinearLayout createCheatToggle(String name, String description,
            boolean initialState, final OnCheatToggleListener listener) {
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

        // Text container
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

        // Toggle switch (using CheckBox styled as switch)
        final CheckBox toggle = new CheckBox(this);
        toggle.setChecked(initialState);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onToggle(isChecked);
            }
        });
        container.addView(toggle);

        return container;
    }

    private LinearLayout createSliderControl(String name, int min, int max,
            int initialValue, final OnSliderChangeListener listener) {
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
        nameText.setText(name);
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameText.setTextColor(COLOR_TEXT);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(nameText);

        final TextView valueText = new TextView(this);
        valueText.setText(String.valueOf(initialValue) + "x");
        valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        valueText.setTextColor(COLOR_ACCENT_LIGHT);
        headerRow.addView(valueText);

        container.addView(headerRow);

        // Slider
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max - min);
        seekBar.setProgress(initialValue - min);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress + 1;
                valueText.setText(String.valueOf(value) + "x");
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
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        button.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(8));
        bg.setColor(bgColor);
        button.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        button.setLayoutParams(params);
        button.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));

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

    /**
     * Toggle menu visibility
     */
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

    /**
     * Refresh menu panel to update toggle states
     */
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
