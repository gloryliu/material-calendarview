package com.adamkoski.calendarwidget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;

import com.adamkoski.library.calendarwidget.R;

import java.util.ArrayList;
import java.util.Calendar;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SUNDAY;

/**
 * Display a month of days
 */
class MonthView extends GridLayout implements View.OnClickListener {

    public static interface Callbacks {

        public void onDateChanged(CalendarDay date);
    }

    private Callbacks callbacks;

    private final ArrayList<WeekDayView> weekDayViews = new ArrayList<>();
    private final ArrayList<DayView> monthDayViews = new ArrayList<>();

    private final Calendar calendarOfRecord = CalendarUtils.copy(Calendar.getInstance());
    private final Calendar tempWorkingCalendar = CalendarUtils.copy(Calendar.getInstance());
    private int firstDayOfWeek = SUNDAY;

    private CalendarDay selection = new CalendarDay(calendarOfRecord);
    private CalendarDay minDate = null;
    private CalendarDay maxDate = null;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setColumnCount(7);
        setRowCount(7);

        int size = getResources().getDimensionPixelSize(R.dimen.cw__default_day_size);

        for(int i = 0; i < 7; i++) {
            WeekDayView dayView = new WeekDayView(getContext(), size);
            addView(dayView);
            weekDayViews.add(dayView);
        }
        setFirstDayOfWeek(firstDayOfWeek);

        for(int i = 0; i < 42; i++) {
            DayView dayView = new DayView(getContext(), size);
            addView(dayView);
            monthDayViews.add(dayView);
            dayView.setOnClickListener(this);
        }
        setSelectedDate(new CalendarDay());
    }

    public void setColor(int color) {
        for(DayView dayView : monthDayViews) {
            dayView.setColor(color);
        }
    }

    private Calendar resetAndGetWorkingCalendar() {
        CalendarUtils.copyDateTo(calendarOfRecord, tempWorkingCalendar);
        int dow = tempWorkingCalendar.get(DAY_OF_WEEK);
        int delta = firstDayOfWeek - dow;
        if(delta >= 0) {
            delta -= 7;
        }
        tempWorkingCalendar.add(DATE, delta);
        return tempWorkingCalendar;
    }

    public void setFirstDayOfWeek(int dayOfWeek) {
        this.firstDayOfWeek = dayOfWeek;

        Calendar calendar = resetAndGetWorkingCalendar();
        calendar.set(DAY_OF_WEEK, dayOfWeek);
        for(WeekDayView dayView : weekDayViews) {
            dayView.setDayOfWeek(calendar.get(DAY_OF_WEEK));
            calendar.add(DATE, 1);
        }
    }

    public void setMinimumDate(CalendarDay minDate) {
        this.minDate = minDate;
        updateUi();
    }

    public void setMaximumDate(CalendarDay maxDate) {
        this.maxDate = maxDate;
        updateUi();
    }

    public void setDate(Calendar calendar) {
        CalendarUtils.copyDateTo(calendar, calendarOfRecord);
        CalendarUtils.setToFirstDay(calendarOfRecord);
        updateUi();
    }


    public void setSelectedDate(Calendar cal) {
        setSelectedDate(new CalendarDay(cal));
    }

    public void setSelectedDate(CalendarDay cal) {
        selection = cal;
        updateUi();
    }

    private void updateUi() {
        int ourMonth = calendarOfRecord.get(MONTH);
        Calendar calendar = resetAndGetWorkingCalendar();
        for(DayView dayView : monthDayViews) {
            CalendarDay day = new CalendarDay(calendar);
            dayView.setDay(day);
            dayView.setActivated(day.getMonth() == ourMonth);
            dayView.setEnabled(day.isInRange(minDate, maxDate));
            dayView.setChecked(day.equals(selection));
            calendar.add(DATE, 1);
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        if(v instanceof DayView) {
            for(DayView other : monthDayViews) {
                other.setChecked(false);
            }
            DayView dayView = (DayView) v;
            dayView.setChecked(true);

            CalendarDay date = dayView.getDate();
            if(date.equals(selection)) {
                return;
            }
            selection = date;

            if(callbacks != null) {
                callbacks.onDateChanged(dayView.getDate());
            }
        }
    }
}
