package candy.castle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LayoutEmail extends LinearLayout {
	
    public LayoutEmail(Context context) {
		super(context);
	}
    
    public LayoutEmail(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public LayoutEmail(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	@Override
    protected void dispatchDraw(Canvas canvas) {       
        Paint panelPaint = new Paint();
        panelPaint.setARGB(250, 4, 77, 140);
        
        RectF baloonRect = new RectF();
        baloonRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
               
        canvas.drawRoundRect(baloonRect, 40, 40, panelPaint);
        
        super.dispatchDraw(canvas);
    }
}