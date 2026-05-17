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
    val category: String,
    val month: YearMonth,
    val members: List<MemberEntity>
) {
    val fileName: String = "jagdish-sports-${category.lowercase(Locale.US)}-${month}.pdf"
}

object MonthlyReportPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 26f

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    private data class PageState(
        val pageNumber: Int,
        val page: PdfDocument.Page,
        val canvas: Canvas,
        var y: Float
    )

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
        val categoryMembers = request.members
            .filter { it.category == request.category }
            .sortedForReport()

        var state = newReportPage(document, request, pageNumber = 1)
        state = drawCategoryTable(
            document = document,
            request = request,
            state = state,
            title = "${request.category} Members",
            members = categoryMembers,
            accentColor = accentColorFor(request.category)
        )

        document.finishPage(state.page)
    }

    private fun newReportPage(
        document: PdfDocument,
        request: MonthlyReportRequest,
        pageNumber: Int
    ): PageState {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val y = drawHeader(page.canvas, request, pageNumber)
        return PageState(pageNumber = pageNumber, page = page, canvas = page.canvas, y = y)
    }

    private fun ensureSpace(
        document: PdfDocument,
        request: MonthlyReportRequest,
        state: PageState,
        requiredHeight: Float
    ): PageState {
        if (state.y + requiredHeight <= PAGE_HEIGHT - MARGIN) {
            return state
        }

        document.finishPage(state.page)
        return newReportPage(document, request, state.pageNumber + 1)
    }

    private fun drawHeader(canvas: Canvas, request: MonthlyReportRequest, pageNumber: Int): Float {
        val members = request.members.filter { it.category == request.category }
        val totalFees = members.sumOf { it.feesPaid }
        val today = LocalDate.now()
        val activeCount = members.count { !it.endDate().isBefore(today) }
        val expiredCount = members.count { it.endDate().isBefore(today) }
        val accentColor = accentColorFor(request.category)

        drawText(canvas, "Jagdish Sports", MARGIN, 46f, 21f, Color.rgb(11, 31, 58), bold = true)
        drawText(canvas, "Gym and Swimming", MARGIN, 68f, 13f, Color.rgb(230, 81, 0), bold = true)
        drawText(
            canvas = canvas,
            text = "${request.category} Monthly Report - ${request.month.format(monthFormatter)}",
            x = MARGIN,
            y = 92f,
            size = 14f,
            color = accentColor,
            bold = true
        )
        drawText(
            canvas = canvas,
            text = "Only ${request.category} records are included | Generated ${LocalDate.now().format(dateFormatter)} | Page $pageNumber",
            x = MARGIN,
            y = 110f,
            size = 8.7f,
            color = Color.DKGRAY
        )

        drawSummaryCard(canvas, MARGIN, 132f, "Category", request.category, "Selected")
        drawSummaryCard(canvas, 168f, 132f, "Members", members.size.toString(), "Total")
        drawSummaryCard(canvas, 300f, 132f, "Fees", "Rs. $totalFees", "Collected")
        drawSummaryCard(canvas, 432f, 132f, "Status", "A $activeCount", "E $expiredCount")

        return 210f
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        label: String,
        value: String,
        helper: String
    ) {
        val width = 118f
        val height = 50f
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(214, 222, 234)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, fill)
        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, border)
        drawText(canvas, label, x + 10f, y + 15f, 8.5f, Color.DKGRAY, bold = true)
        drawText(canvas, value, x + 10f, y + 31f, 12f, Color.rgb(230, 81, 0), bold = true)
        drawText(canvas, helper, x + 10f, y + 44f, 8.2f, Color.rgb(60, 70, 82))
    }

    private fun drawCategoryTable(
        document: PdfDocument,
        request: MonthlyReportRequest,
        state: PageState,
        title: String,
        members: List<MemberEntity>,
        accentColor: Int
    ): PageState {
        var current = ensureSpace(document, request, state, 86f)
        drawSectionTitle(current.canvas, current.y, title, members.size, accentColor)
        current.y += 34f
        drawTableHeader(current.canvas, current.y, accentColor)
        current.y += ROW_HEIGHT

        if (members.isEmpty()) {
            current = ensureSpace(document, request, current, ROW_HEIGHT)
            drawEmptyRow(current.canvas, current.y, "No records found.")
            current.y += ROW_HEIGHT
            return current
        }

        members.forEachIndexed { index, member ->
            if (current.y + ROW_HEIGHT > PAGE_HEIGHT - MARGIN) {
                document.finishPage(current.page)
                current = newReportPage(document, request, current.pageNumber + 1)
                drawSectionTitle(
                    canvas = current.canvas,
                    y = current.y,
                    title = "$title (continued)",
                    count = members.size - index,
                    accentColor = accentColor
                )
                current.y += 34f
                drawTableHeader(current.canvas, current.y, accentColor)
                current.y += ROW_HEIGHT
            }
            drawMemberRow(current.canvas, current.y, index + 1, member)
            current.y += ROW_HEIGHT
        }
        return current
    }

    private fun drawSectionTitle(
        canvas: Canvas,
        y: Float,
        title: String,
        count: Int,
        accentColor: Int
    ) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 24f, 7f, 7f, fill)
        drawText(canvas, title, MARGIN + 12f, y + 16f, 10.5f, Color.WHITE, bold = true)
        drawText(canvas, "$count records", PAGE_WIDTH - MARGIN - 82f, y + 16f, 9f, Color.WHITE, bold = true)
    }

    private fun drawTableHeader(canvas: Canvas, y: Float, accentColor: Int) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(232, 238, 245)
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT, fill)
        drawRowBorder(canvas, y, accentColor)
        drawText(canvas, "#", 43f, y + 17f, 8.5f, Color.BLACK, true)
        drawText(canvas, "Name", 62f, y + 17f, 8.5f, Color.BLACK, true)
        drawText(canvas, "Phone", 176f, y + 17f, 8.5f, Color.BLACK, true)
        drawText(canvas, "Start Date", 254f, y + 17f, 8.5f, Color.BLACK, true)
        drawText(canvas, "End Date", 326f, y + 17f, 8.5f, Color.BLACK, true)
        drawText(canvas, "Fees", 402f, y + 17f, 8.5f, Color.BLACK, true)
        drawText(canvas, "Status", 462f, y + 17f, 8.5f, Color.BLACK, true)
    }

    private fun drawMemberRow(canvas: Canvas, y: Float, index: Int, member: MemberEntity) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT, fill)
        drawRowBorder(canvas, y, Color.rgb(214, 222, 234))

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.3f
            color = Color.rgb(25, 32, 44)
        }
        drawText(canvas, index.toString(), 43f, y + 17f, 8.3f, Color.DKGRAY)
        drawText(canvas, member.fullName.ellipsize(textPaint, 104f), 62f, y + 17f, 8.3f, Color.BLACK)
        drawText(canvas, member.phoneNumber.ellipsize(textPaint, 70f), 176f, y + 17f, 8.3f, Color.BLACK)
        drawText(canvas, member.startDate().format(dateFormatter), 254f, y + 17f, 8.3f, Color.BLACK)
        drawText(canvas, member.endDate().format(dateFormatter), 326f, y + 17f, 8.3f, Color.BLACK)
        drawText(canvas, "Rs. ${member.feesPaid}", 402f, y + 17f, 8.3f, Color.BLACK)
        drawText(
            canvas = canvas,
            text = member.status(expiringSoonDays = 7).name.replace('_', ' '),
            x = 462f,
            y = y + 17f,
            size = 8.3f,
            color = Color.BLACK
        )
    }

    private fun drawEmptyRow(canvas: Canvas, y: Float, message: String) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT, fill)
        drawRowBorder(canvas, y, Color.rgb(214, 222, 234))
        drawText(canvas, message, MARGIN + 10f, y + 17f, 8.6f, Color.DKGRAY)
    }

    private fun drawRowBorder(canvas: Canvas, y: Float, color: Int) {
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT, border)
        listOf(58f, 172f, 250f, 322f, 398f, 458f).forEach { x ->
            canvas.drawLine(x, y, x, y + ROW_HEIGHT, border)
        }
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

    private fun List<MemberEntity>.sortedForReport(): List<MemberEntity> {
        return sortedWith(
            compareBy<MemberEntity> { it.startDateEpochDay }
                .thenBy { it.fullName.lowercase(Locale.getDefault()) }
        )
    }

    private fun accentColorFor(category: String): Int {
        return if (category == MemberCategories.GYM) {
            Color.rgb(11, 31, 58)
        } else {
            Color.rgb(230, 81, 0)
        }
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
