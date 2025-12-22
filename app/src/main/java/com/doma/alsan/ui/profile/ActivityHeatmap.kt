package com.doma.alsan.ui.profile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.ColorUtils
import com.doma.alsan.R
import com.doma.alsan.data.response.anilist.UserActivityHistory
import android.widget.PopupWindow
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup
import android.graphics.drawable.GradientDrawable
import java.util.Calendar

class ActivityHeatmap @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Layout parameters
    private val cellSize = dpToPx(10f)
    private val cellSpacing = dpToPx(3f)
    private val cellRadius = dpToPx(2f)
    private val numRows = 7
    private var numCols = 53 // Default, updated onMeasure
    private var activeColors: List<Int> = listOf()

    // Data interaction
    data class CellData(val level: Int, val amount: Int, val date: Long)
    private var dataMap: Map<Long, CellData> = emptyMap()

    private var popupWindow: PopupWindow? = null
    private var popupView: TextView? = null
    
    // Interaction state
    private var hoveredCol = -1
    private var hoveredRow = -1
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = dpToPx(10f)
        textAlign = Paint.Align.CENTER
    }
    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC000000") // Semi-transparent black
        style = Paint.Style.FILL
    }
    private val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())

    init {
        initColors(context)
        if (isInEditMode) {
             // Preview colors
             activeColors = listOf(Color.LTGRAY, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN)
        }
    }

    private fun initColors(context: Context) {
        val typedValue = TypedValue()
        
        // Primary Color
        var primary = Color.GREEN
        if (context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)) {
            primary = typedValue.data
        } else if (context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            primary = typedValue.data
        }

        // Empty Color (Theme Content Color with low alpha)
        var content = Color.GRAY
        if (context.theme.resolveAttribute(R.attr.themeContentColor, typedValue, true)) {
            content = typedValue.data
        } else if (context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)) {
            content = typedValue.data
        }
        val emptyColor = ColorUtils.setAlphaComponent(content, 26) // ~10% opacity

        // Generate levels using primary color with better visibility
        activeColors = listOf(
            emptyColor, // 0
            ColorUtils.setAlphaComponent(primary, 77),  // 30%
            ColorUtils.setAlphaComponent(primary, 128), // 50%
            ColorUtils.setAlphaComponent(primary, 179), // 70%
            primary // 100%
        )
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    fun setData(history: List<UserActivityHistory>) {
        val maxAmount = history.maxOfOrNull { it.amount } ?: 0
        
        dataMap = history.associate { item ->
            val days = item.date.toLong() / 86400
            
            val level = if (item.amount <= 0) 0
            else {
                val maxSqrt = kotlin.math.sqrt(maxAmount.toDouble()).toFloat()
                val itemSqrt = kotlin.math.sqrt(item.amount.toDouble()).toFloat()
                val ratio = if (maxSqrt > 0) itemSqrt / maxSqrt else 0f
                when {
                    ratio <= 0.25f -> 1
                    ratio <= 0.50f -> 2
                    ratio <= 0.75f -> 3
                    else -> 4
                }
            }
            
            days to CellData(level, item.amount, item.date.toLong() * 1000) 
        }
        
        requestLayout()
        invalidate()
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_MOVE -> {
                val totalCellSize = cellSize + cellSpacing
                // Calculate clicked cell
                val col = ((event.x - paddingLeft) / totalCellSize).toInt()
                val row = ((event.y - paddingTop) / totalCellSize).toInt()
                
                if (col in 0 until numCols && row in 0 until numRows) {
                    val cellLeft = paddingLeft + col * totalCellSize
                    val cellTop = paddingTop + row * totalCellSize
                    
                    if (event.x >= cellLeft && event.x <= cellLeft + cellSize &&
                        event.y >= cellTop && event.y <= cellTop + cellSize) {
                            
                        // Update if changed or not showing
                        if (col != hoveredCol || row != hoveredRow || popupWindow?.isShowing != true) {
                            if (col != hoveredCol || row != hoveredRow) {
                                performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            hoveredCol = col
                            hoveredRow = row
                            invalidate()
                            updatePopup(col, row)
                        }
                    } 
                } else {
                    if (hoveredCol != -1) {
                         hoveredCol = -1
                         hoveredRow = -1
                         invalidate()
                         dismissPopup()
                    }
                }
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                hoveredCol = -1
                hoveredRow = -1
                invalidate()
                dismissPopup()
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        
        val effectiveWidth = if (widthMode == MeasureSpec.UNSPECIFIED) {
             (53 * (cellSize + cellSpacing) - cellSpacing).toInt() + paddingLeft + paddingRight
        } else {
             widthSize
        }

        val availableContentWidth = effectiveWidth - paddingLeft - paddingRight
        if (availableContentWidth > 0) {
            numCols = (availableContentWidth / (cellSize + cellSpacing)).toInt()
        }
        
        val height = (numRows * (cellSize + cellSpacing) - cellSpacing).toInt() + paddingTop + paddingBottom
        setMeasuredDimension(effectiveWidth, resolveSize(height, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val utc = java.util.TimeZone.getTimeZone("UTC")
        val todayCal = Calendar.getInstance(utc)
        
        val lastColMonday = todayCal.clone() as Calendar
        while (lastColMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            lastColMonday.add(Calendar.DAY_OF_YEAR, -1)
        }
        val lastColMondayDays = lastColMonday.timeInMillis / 1000 / 86400
        val startDays = lastColMondayDays - ((numCols - 1) * 7)
        val todayDays = todayCal.timeInMillis / 1000 / 86400
        
        var hoveredRect: android.graphics.RectF? = null
        var hoveredData: CellData? = null
        var hoveredDayMillis: Long = 0

        for (col in 0 until numCols) {
            for (row in 0 until numRows) {
               val offsetDays = col * 7 + row
               val currentDays = startDays + offsetDays
               
               if (currentDays > todayDays) continue 

               val data = dataMap[currentDays]
               val level = data?.level ?: 0
               
               paint.color = getColorForLevel(level)
               
               val left = paddingLeft + col * (cellSize + cellSpacing)
               val top = paddingTop + row * (cellSize + cellSpacing)
               val rect = android.graphics.RectF(left, top, left + cellSize, top + cellSize)
               
               if (col == hoveredCol && row == hoveredRow) {
                   // Save for later drawing to be on top
                   hoveredRect = rect
                   hoveredData = data
                   // Calculate date for empty cells too
                   hoveredDayMillis = currentDays * 86400 * 1000 
               } else {
                   canvas.drawRoundRect(rect, cellRadius, cellRadius, paint)
               }
            }
        }
        
        // Draw hovered cell on top with Zoom
        if (hoveredRect != null) {
            val cx = hoveredRect.centerX()
            val cy = hoveredRect.centerY()
            // Zoom 1.4x
            val zoomedSize = cellSize * 1.4f
            val zLeft = cx - zoomedSize / 2
            val zTop = cy - zoomedSize / 2
            
            val level = hoveredData?.level ?: 0
            paint.color = getColorForLevel(level)
            
            // Draw shadow/stroke for visibility?
            // Simple zoomed rect
            canvas.drawRoundRect(zLeft, zTop, zLeft + zoomedSize, zTop + zoomedSize, cellRadius * 1.4f, cellRadius * 1.4f, paint)
            
            // Tooltip handled by PopupWindow
        }
    }
    
    private fun drawTooltip(canvas: Canvas, x: Float, y: Float, data: CellData?, dayMillis: Long) {
        val amount = data?.amount ?: 0
        val dateStr = try {
            val date = if (data != null) java.util.Date(data.date) else java.util.Date(dayMillis)
            dateFormat.format(date)
        } catch (e: Exception) { "" }
        
        val text = "$dateStr: $amount"
        
        val padding = dpToPx(4f)
        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.descent() - textPaint.ascent()
        
        val boxWidth = textWidth + padding * 4
        val boxHeight = textHeight + padding * 2
        
        // Position above the cell
        var boxLeft = x - boxWidth / 2
        var boxTop = y - boxHeight - dpToPx(24f) // Margin above
        
        // Adjust if out of bounds
        if (boxLeft < 0) boxLeft = 0f
        if (boxLeft + boxWidth > width) boxLeft = width - boxWidth
        
        // If out of top, let it overflow (clipChildren=false required in parent)
        // if (boxTop < 0) ... removed
        
        val rect = android.graphics.RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight)
        canvas.drawRoundRect(rect, dpToPx(4f), dpToPx(4f), tooltipPaint)
        
        // Text centered in box
        // y is baseline.
        val textY = boxTop + padding + -textPaint.ascent()
        canvas.drawText(text, boxLeft + boxWidth / 2, textY, textPaint)
    }

    private fun getColorForLevel(level: Int): Int {
        val index = if (level in 0..4) level else if (level > 4) 4 else 0
        return activeColors.getOrElse(index) { Color.GRAY }
    }

    private fun getDataAt(col: Int, row: Int): CellData? {
        val utc = java.util.TimeZone.getTimeZone("UTC")
        val todayCal = Calendar.getInstance(utc)
        val lastColMonday = todayCal.clone() as Calendar
        while (lastColMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            lastColMonday.add(Calendar.DAY_OF_YEAR, -1)
        }
        val lastColMondayDays = lastColMonday.timeInMillis / 1000 / 86400
        val startDays = lastColMondayDays - ((numCols - 1) * 7)
        val offsetDays = col * 7 + row
        val currentDays = startDays + offsetDays
        return dataMap[currentDays]
    }

    private fun updatePopup(col: Int, row: Int) {
        val data = getDataAt(col, row)
        val amount = data?.amount ?: 0
        
        // Calculate date
        val utc = java.util.TimeZone.getTimeZone("UTC")
        val todayCal = Calendar.getInstance(utc)
        val lastColMonday = todayCal.clone() as Calendar
        while (lastColMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
           lastColMonday.add(Calendar.DAY_OF_YEAR, -1)
        }
        val lastColMondayDays = lastColMonday.timeInMillis / 1000 / 86400
        val startDays = lastColMondayDays - ((numCols - 1) * 7)
        val currentDays = startDays + (col * 7 + row)
        val dateMillis = if (data != null) data.date else currentDays * 86400 * 1000
        
        val dateStr = try {
            dateFormat.format(java.util.Date(dateMillis))
        } catch (e: Exception) { "" }
        val text = "$dateStr: $amount"

        if (popupWindow == null) {
             val context = context
             popupView = TextView(context).apply {
                  setTextColor(Color.WHITE)
                  textSize = 12f
                  setPadding(dpToPx(8f).toInt(), dpToPx(4f).toInt(), dpToPx(8f).toInt(), dpToPx(4f).toInt())
                  val shape = GradientDrawable()
                  shape.setColor(Color.parseColor("#CC000000"))
                  shape.cornerRadius = dpToPx(4f)
                  background = shape
             }
             popupWindow = PopupWindow(popupView, 
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT
             ).apply {
                 animationStyle = 0
             }
        }
        
        popupView?.text = text
        popupView?.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val w = popupView?.measuredWidth ?: 0
        val h = popupView?.measuredHeight ?: 0
        
        // Coordinates
        val left = paddingLeft + col * (cellSize + cellSpacing)
        val top = paddingTop + row * (cellSize + cellSpacing)
        val centerX = left + cellSize / 2
        val topY = top

        val loc = IntArray(2)
        getLocationOnScreen(loc)
        val screenX = loc[0] + centerX
        val screenY = loc[1] + topY

        val xPos = (screenX - w / 2).toInt()
        val yPos = (screenY - h - dpToPx(12f)).toInt()

        if (popupWindow?.isShowing == true) {
            popupWindow?.update(xPos, yPos, w, h)
        } else {
            popupWindow?.showAtLocation(this, Gravity.NO_GRAVITY, xPos, yPos)
        }
    }

    private fun dismissPopup() {
        popupWindow?.dismiss()
    }
}
