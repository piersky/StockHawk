package com.pierluigipapeschi.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.pierluigipapeschi.stockhawk.R;
import com.pierluigipapeschi.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by pier on 28/03/17.
 */

public class StockDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private String mSymbol;

    private static final int STOCK_LOADER = 1;
    //FragmentDetailsAdapter mAdapter;
    private boolean viewsCreated;

    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;

    @BindView(R.id.tv_symbol_frag)
    TextView mTVSymbol;
    @BindView(R.id.tv_stock_current_price_frag)
    TextView mTVPrice;
    @BindView(R.id.linechart_frag)
    LineChart mStockChart;

    public StockDetailsFragment() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(getString(R.string.SYMBOL_EXTRA))) {
            mSymbol = getArguments().getString(getString(R.string.SYMBOL_EXTRA));
            Timber.d("onCreate(): " + mSymbol);

            getActivity().getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_details, container, false);

        ButterKnife.bind(this, rootView);

        viewsCreated = true;

        Timber.d("onCreateView()");

        if (mCursor != null) setViews();
        /*if (mSymbol != null) {
            if (mCursor != null) {
                setViews();
            }
        }*/
        return rootView;
    }

    private void setViews() {
        Timber.d("setViews()" + viewsCreated);

        if (viewsCreated) {
            mTVSymbol.setText(mSymbol);

            if (mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE)) > 0) {
                mTVPrice.setText(dollarFormatWithPlus.format(mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote.COLUMN_PRICE))));
                mTVPrice.setTextColor(ResourcesCompat.getColor(getResources(), R.color.material_green_700, null));
            } else {
                mTVPrice.setText(dollarFormat.format(mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote.COLUMN_PRICE))));
                mTVPrice.setTextColor(ResourcesCompat.getColor(getResources(), R.color.material_red_700, null));
            }

            LineData lineData = new LineData(getHistoricalData(mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY))));

            XAxis xAxis = mStockChart.getXAxis();
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) value);
                    return String.valueOf(calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR));
                }
            });

            mStockChart.getAxisRight().setDrawLabels(false);

            Description description = new Description();
            description.setText(getString(R.string.details_chart_label));
            mStockChart.setDescription(description);
            mStockChart.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight, null));

            mStockChart.setData(lineData);
            mStockChart.animateX(2000);
            mStockChart.invalidate();

            viewsCreated = false;
        }
    }

    private List<ILineDataSet> getHistoricalData(String history) {

        List<Entry> entryList = new ArrayList<>();
        List<String> sList = new ArrayList<>(Arrays.asList(history.split("\n")));
        List<String> xAxisValues = new ArrayList<>();

        for (String s : sList) {
            String[] splitted = s.split(",");
            entryList.add(new Entry(Float.parseFloat(splitted[0]), Float.parseFloat(splitted[1])));
            xAxisValues.add(splitted[0]);
        }
        Collections.reverse(entryList);

        LineDataSet setStock = new LineDataSet(entryList, mSymbol);
        setStock.setAxisDependency(YAxis.AxisDependency.LEFT);
        setStock.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        setStock.setCircleColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setStock);

        return dataSets;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader()");

        return new CursorLoader(getContext(), Contract.Quote.makeUriForStock(mSymbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Timber.d("onLoadFinished()");

        mCursor = data;
        mCursor.moveToFirst();
        setViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Timber.d("onLoaderReset()");
    }
}
