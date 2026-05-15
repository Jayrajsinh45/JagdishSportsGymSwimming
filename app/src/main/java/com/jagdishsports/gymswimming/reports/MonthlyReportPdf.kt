package com.jagdishsports.gymswimming.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.jagdishsports.gymswimming.data.MemberCategories
import com.jagdishsports.gymswimming.data.MemberEntity
import com.jagdishsports.gymswimming.data.endDate
import com.jagdishsports.gymswimming.data.startDate
import com.jagdishsports.gymswimming.data.status
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyReportRequest(
    val month: YearMonth,
    val members: List<MemberEntity>
) {
    val fileName: String = "jagdish-sports-${month}.pdf"
}

object MonthlyReportPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 25f

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    fun write(context: Context, uri: Uri, request: MonthlyReportRequest) {
        val document = PdfDocument()
        try {
            renderDocument(document, request)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                document.writeTo(output)
            } ?: error("Unable to open the selected file.")
        } finally {
            document.close()
        }
    }

    private fun renderDocument(document: PdfDocument, request: MonthlyReportRequest) {
        val rows = request.members.sortedWith(
            compareBy<MemberEntity> { it.startDateEpochDay }
                .thenBy { it.fullName.lowercase(Locale.getDefault()) }
        )
        var pageNumber = 1
        var page = newPage(document, pageNumber)
        var canvas = page.canvas
        var y = drawHeader(canvas, request, pageNumber)
        y = drawTableHeader(canvas, y)

        if (rows.isEmpty()) {
            drawText(
                canvas = canvas,
                text = "No member records found for this month.",
                x = MARGIN,
                y = y + 28f,
                size = 11f,
                color = Color.DKGRAY
            )
        } else {
            rows.forEachIndexed { index, member ->
                if (y + ROW_HEIGHT > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber += 1
                    page = newPage(document, pageNumber)
                    canvas = page.canvas
                    y = drawHeader(canvas, request, pageNumber)
                    y = drawTableHeader(canvas, y)
                }
                y = drawMemberRow(canvas, y, index + 1, member)
            }
        }

        document.finishPage(page)
    }

    private fun newPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        return document.startPage(pageInfo)
    }

    private fun drawHeader(
        canvas: Canvas,
        request: MonthlyReportRequest,
        pageNumber: Int
    ): Float {
        val members = request.members
        val gymCount = members.count { it.category == MemberCategories.GYM }
        val swimmingCount = members.count { it.category == MemberCategories.SWIMMING }
        val totalFees = members.sumOf { it.feesPaid }
        val today = LocalDate.now()
        val activeCount = members.count { !it.endDate().isBefore(today) }
        val expiredCount = members.count { it.endDate().isBefore(today) }

        drawText(
            canvas = canvas,
            text = "Jagdish Sports Gym and Swimming",
            x = MARGIN,
            y = 48f,
            size = 18f,
            color = Color.rgb(11, 31, 58),
            bold = true
        )
        drawText(
            canvas = canvas,
            text = "Monthly Member Report - ${request.month.format(monthFormatter)}",
            x = MARGIN,
            y = 72f,
            size = 12f,
            color = Color.rgb(230, 81, 0),
            bold = true
        )
        drawText(
            canvas = canvas,
            text = "Records grouped by membership start date | Generated ${LocalDate.now().format(dateFormatter)} | Page $pageNumber",
            x = MARGIN,
            y = 92f,
            size = 9f,
            color = Color.DKGRAY
        )

        drawText(canvas, "Total: ${members.size}", MARGIN, 122f, 10f, Color.BLACK, true)
        drawText(canvas, "Gym: $gymCount", 128f, 122f, 10f, Color.BLACK, true)
        drawText(canvas, "Swimming: $swimmingCount", 210f, 122f, 10f, Color.BLACK, true)
        drawText(canvas, "Active: $activeCount", 330f, 122f, 10f, Color.BLACK, true)
        drawText(canvas, "Expired: $expiredCount", 420f, 122f, 10f, Color.BLACK, true)
        drawText(canvas, "Fees: Rs. $totalFees", MARGIN, 144f, 10f, Color.BLACK, true)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(11, 31, 58)
            strokeWidth = 1.5f
        }
        canvas.drawLine(MARGIN, 162f, PAGE_WIDTH - MARGIN, 162f, paint)
        return 186f
    }

    private fun drawTableHeader(canvas: Canvas, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(232, 238, 245)
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y - 16f, PAGE_WIDTH - MARGIN, y + 8f, paint)

        drawText(canvas, "#", 42f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "Name", 62f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "Phone", 162f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "Category", 236f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "Start", 304f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "End", 370f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "Fees", 436f, y, 8.5f, Color.BLACK, true)
        drawText(canvas, "Status", 486f, y, 8.5f, Color.BLACK, true)
        return y + ROW_HEIGHT
    }

    private fun drawMemberRow(canvas: Canvas, y: Float, index: Int, member: MemberEntity): Float {
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y - 16f, PAGE_WIDTH - MARGIN, y + 8f, rowPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.3f
            color = Color.rgb(25, 32, 44)
        }

        drawText(canvas, index.toString(), 42f, y, 8.3f, Color.DKGRAY)
        drawText(canvas, member.fullName.ellipsize(textPaint, 92f), 62f, y, 8.3f, Color.BLACK)
        drawText(canvas, member.phoneNumber.ellipsize(textPaint, 66f), 162f, y, 8.3f, Color.BLACK)
        drawText(canvas, member.category, 236f, y, 8.3f, Color.BLACK)
        drawText(canvas, member.startDate().format(dateFormatter), 304f, y, 8.3f, Color.BLACK)
        drawText(canvas, member.endDate().format(dateFormatter), 370f, y, 8.3f, Color.BLACK)
        drawText(canvas, "Rs. ${member.feesPaid}", 436f, y, 8.3f, Color.BLACK)
        drawText(
            canvas = canvas,
            text = member.status(expiringSoonDays = 7).name.replace('_', ' '),
            x = 486f,
            y = y,
            size = 8.3f,
            color = Color.BLACK
        )
        return y + ROW_HEIGHT
    }

    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        canvas.drawText(text, x, y, paint)
    }

    private fun String.ellipsize(paint: Paint, maxWidth: Float): String {
        if (paint.measureText(this) <= maxWidth) {
            return this
        }

        var candidate = this
        while (candidate.length > 1 && paint.measureText("$candidate...") > maxWidth) {
            candidate = candidate.dropLast(1)
        }
        return "$candidate..."
    }
}
