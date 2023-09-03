package com.crow.module_book.compose.comic.reader

import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventInterval
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo

@Composable
fun IntentButtonContent(reader: ReaderPrevNextInfo, iEvent: BaseIEventInterval<ReaderPrevNextInfo>) {
    Button(
        onClick = {
            BaseEvent.getSIngleInstance().doOnInterval {
                iEvent.onIntervalOk(BaseEventEntity(reader, it.mBaseEvent))
            }
        },
        elevation = ButtonDefaults.elevation(defaultElevation = 20.dp),
        border = BorderStroke(1.dp, Color.Gray),
        colors = ButtonDefaults.buttonColors(Color(parseColor("#e6fffb"))),
        modifier = Modifier.shadow(8.dp)
    ) {
        Text(
            text = reader.mInfo,
            fontSize = 16.sp,
            modifier = Modifier.padding(
                top = 10.dp,
                bottom = 10.dp,
            ),
            style = TextStyle.Default.copy(
                letterSpacing = 5.sp,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(1f, 1f),
                    blurRadius = 3f
                )
            ),
        )
    }
}