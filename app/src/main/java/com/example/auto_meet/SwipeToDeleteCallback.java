package com.example.auto_meet;

import com.example.auto_meet_zoom.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auto_meet.adapter.MeetingAdapter;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private MeetingAdapter adapter;
    private Drawable icon;
    private final ColorDrawable background;
    private final Paint paint;
    private final float cornerRadius = 40f;
    private final float scaleFactor = 1.3f;

    public SwipeToDeleteCallback(MeetingAdapter adapter, Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
        icon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        background = new ColorDrawable(Color.GRAY);

        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        adapter.deleteItem(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int iconMargin = (itemView.getHeight() - (int) (icon.getIntrinsicHeight() * scaleFactor)) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - (int) (icon.getIntrinsicHeight() * scaleFactor)) / 2;

        Bitmap bitmap = drawableToBitmap(icon);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) (icon.getIntrinsicWidth() * scaleFactor), (int) (icon.getIntrinsicHeight() * scaleFactor), false);

        if (dX < 0) {
            int iconLeft = itemView.getRight() - iconMargin - scaledBitmap.getWidth();

            RectF backgroundRect = new RectF(
                    itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());

            Path path = new Path();
            path.addRoundRect(backgroundRect, cornerRadius, cornerRadius, Path.Direction.CW);
            c.drawPath(path, paint);

            c.drawBitmap(scaledBitmap, iconLeft, iconTop, null);
        } else {
            background.setBounds(0, 0, 0, 0);
        }

        bitmap.recycle();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
