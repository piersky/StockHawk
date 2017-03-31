package com.pierluigipapeschi.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.pierluigipapeschi.stockhawk.R;
import com.pierluigipapeschi.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by pier on 21/03/17.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(getApplicationContext(), intent);
    }


    class StockRemoteViewsFactory implements RemoteViewsFactory {

        private Cursor cursor;
        private Context context;
        private final DecimalFormat dollarFormatWithPlus;
        private final DecimalFormat dollarFormat;

        public StockRemoteViewsFactory(Context c, Intent i) {

            context = c;

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
        }

        @Override
        public void onCreate() {
            Timber.d("onCreate()");
        }

        @Override
        public void onDataSetChanged() {

            if (cursor != null)
                cursor.close();

            final long identityToken = Binder.clearCallingIdentity();
            Uri stocksUri = Contract.Quote.URI;
            cursor = context.getContentResolver().query(stocksUri,
                    new String[]{
                            Contract.Quote.COLUMN_SYMBOL,
                            Contract.Quote.COLUMN_PRICE,
                            Contract.Quote.COLUMN_ABSOLUTE_CHANGE
                    },
                    null,
                    null,
                    null);

            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (cursor != null) cursor.close();
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_stock_item);

            if (cursor.moveToPosition(position)) {
                remoteViews.setTextViewText(R.id.tv_symbol_widget_item, cursor.getString(0));
                remoteViews.setTextViewText(R.id.tv_price_widget_item, dollarFormat.format(Float.parseFloat(cursor.getString(1))));

                if (Float.parseFloat(cursor.getString(2)) > 0) {
                    remoteViews.setTextColor(R.id.tv_price_widget_item, ResourcesCompat.getColor(getResources(), R.color.material_green_700, null));
                    remoteViews.setTextViewText(R.id.tv_change_item, dollarFormatWithPlus.format(Float.parseFloat(cursor.getString(2))));
                    remoteViews.setTextColor(R.id.tv_change_item, ResourcesCompat.getColor(getResources(), R.color.material_green_700, null));
                } else {
                    remoteViews.setTextColor(R.id.tv_price_widget_item, ResourcesCompat.getColor(getResources(), R.color.material_red_700, null));
                    remoteViews.setTextViewText(R.id.tv_change_item, dollarFormat.format(Float.parseFloat(cursor.getString(2))));
                    remoteViews.setTextColor(R.id.tv_change_item, ResourcesCompat.getColor(getResources(), R.color.material_red_700, null));
                }

                Bundle extras = new Bundle();
                extras.putString(getString(R.string.SYMBOL_EXTRA), cursor.getString(0));

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
            }
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
