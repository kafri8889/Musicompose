package com.anafthdev.musicompose.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.android.material.math.MathUtils.lerp
import kotlin.math.floor
import kotlin.math.roundToInt

@Preview
@Composable
private fun CircularCheckboxPreview() {
	Column {
		CircularCheckbox(
			checked = true,
			onCheckedChange = {}
		)
		
		CircularCheckbox(
			checked = false,
			onCheckedChange = {}
		)
	}
}

/**
 * CircularCheckbox, copy from [androidx.compose.material.Checkbox]
 * @author kafri8889
 */
@Composable
fun CircularCheckbox(
	checked: Boolean,
	onCheckedChange: ((Boolean) -> Unit)?,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	colors: CheckboxColors = CheckboxDefaults.colors()
) {
	TriStateCheckbox(
		state = ToggleableState(checked),
		onClick = if (onCheckedChange != null) { { onCheckedChange(!checked) } } else null,
		interactionSource = interactionSource,
		enabled = enabled,
		colors = colors,
		modifier = modifier
	)
}

@Composable
private fun TriStateCheckbox(
	state: ToggleableState,
	onClick: (() -> Unit)?,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	colors: CheckboxColors = CheckboxDefaults.colors()
) {
	val toggleableModifier =
		if (onClick != null) {
			Modifier.triStateToggleable(
				state = state,
				onClick = onClick,
				enabled = enabled,
				role = Role.Checkbox,
				interactionSource = interactionSource,
				indication = rememberRipple(
					bounded = false,
					radius = CheckboxRippleRadius
				)
			)
		} else {
			Modifier
		}
	CircularCheckboxImpl(
		enabled = enabled,
		value = state,
		modifier = modifier
			.then(
				if (onClick != null) {
					Modifier.minimumTouchTargetSize()
				} else {
					Modifier
				}
			)
			.then(toggleableModifier)
			.padding(CheckboxDefaultPadding),
		colors = colors
	)
}

@Composable
private fun CircularCheckboxImpl(
	enabled: Boolean,
	value: ToggleableState,
	modifier: Modifier,
	colors: CheckboxColors
) {
	
	val transition = updateTransition(value, label = "")
	val checkDrawFraction by transition.animateFloat(
		transitionSpec = {
			when {
				initialState == ToggleableState.Off -> tween(CheckAnimationDuration)
				targetState == ToggleableState.Off -> snap(BoxOutDuration)
				else -> spring()
			}
		}, label = ""
	) {
		when (it) {
			ToggleableState.On -> 1f
			ToggleableState.Off -> 0f
			ToggleableState.Indeterminate -> 1f
		}
	}
	
	val checkCenterGravitationShiftFraction by transition.animateFloat(
		transitionSpec = {
			when {
				initialState == ToggleableState.Off -> snap()
				targetState == ToggleableState.Off -> snap(BoxOutDuration)
				else -> tween(durationMillis = CheckAnimationDuration)
			}
		}, label = ""
	) {
		when (it) {
			ToggleableState.On -> 0f
			ToggleableState.Off -> 0f
			ToggleableState.Indeterminate -> 1f
		}
	}
	
	val checkCache = remember { CheckDrawingCache() }
	val checkColor by colors.checkmarkColor(value)
	val boxColor by colors.boxColor(enabled, value)
	val borderColor by colors.borderColor(enabled, value)
	Canvas(
		modifier
			.wrapContentSize(Alignment.Center)
			.requiredSize(CheckboxSize)) {
		val strokeWidthPx = floor(2.dp.toPx())
		drawBox(
			boxColor = boxColor,
			borderColor = borderColor,
			radius = RadiusSize.toPx(),
			strokeWidth = strokeWidthPx
		)
		drawCheck(
			checkColor = checkColor,
			checkFraction = checkDrawFraction,
			crossCenterGravitation = checkCenterGravitationShiftFraction,
			strokeWidthPx = strokeWidthPx,
			drawingCache = checkCache
		)
	}
}

@Immutable
private class CheckDrawingCache(
	val checkPath: Path = Path(),
	val pathMeasure: PathMeasure = PathMeasure(),
	val pathToDraw: Path = Path()
)

private fun DrawScope.drawBox(
	boxColor: Color,
	borderColor: Color,
	radius: Float,
	strokeWidth: Float
) {
	val halfStrokeWidth = strokeWidth / 2.0f
	val stroke = Stroke(strokeWidth)
	val checkboxSize = size.width
	drawRoundRect(
		boxColor,
		topLeft = Offset(strokeWidth, strokeWidth),
		size = Size(checkboxSize - strokeWidth * 2, checkboxSize - strokeWidth * 2),
		cornerRadius = CornerRadius(100f),
		style = Fill
	)
	drawRoundRect(
		borderColor,
		topLeft = Offset(halfStrokeWidth, halfStrokeWidth),
		size = Size(checkboxSize - strokeWidth, checkboxSize - strokeWidth),
		cornerRadius = CornerRadius(100f),
		style = stroke
	)
}

private fun DrawScope.drawCheck(
	checkColor: Color,
	checkFraction: Float,
	crossCenterGravitation: Float,
	strokeWidthPx: Float,
	drawingCache: CheckDrawingCache
) {
	val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Square)
	val width = size.width
	val checkCrossX = 0.4f
	val checkCrossY = 0.7f
	val leftX = 0.2f
	val leftY = 0.5f
	val rightX = 0.8f
	val rightY = 0.3f
	
	val gravitatedCrossX = lerp(checkCrossX, 0.5f, crossCenterGravitation)
	val gravitatedCrossY = lerp(checkCrossY, 0.5f, crossCenterGravitation)
	// gravitate only Y for end to achieve center line
	val gravitatedLeftY = lerp(leftY, 0.5f, crossCenterGravitation)
	val gravitatedRightY = lerp(rightY, 0.5f, crossCenterGravitation)
	
	with(drawingCache) {
		checkPath.reset()
		checkPath.moveTo(width * leftX, width * gravitatedLeftY)
		checkPath.lineTo(width * gravitatedCrossX, width * gravitatedCrossY)
		checkPath.lineTo(width * rightX, width * gravitatedRightY)
		pathMeasure.setPath(checkPath, false)
		pathToDraw.reset()
		pathMeasure.getSegment(
			0f, pathMeasure.length * checkFraction, pathToDraw, true
		)
	}

	drawPath(drawingCache.pathToDraw, checkColor, style = stroke)
}

private class MinimumTouchTargetModifier(val size: DpSize) : LayoutModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints
	): MeasureResult {
		
		val placeable = measurable.measure(constraints)
		
		// Be at least as big as the minimum dimension in both dimensions
		val width = maxOf(placeable.width, size.width.roundToPx())
		val height = maxOf(placeable.height, size.height.roundToPx())
		
		return layout(width, height) {
			val centerX = ((width - placeable.width) / 2f).roundToInt()
			val centerY = ((height - placeable.height) / 2f).roundToInt()
			placeable.place(centerX, centerY)
		}
	}
	
	override fun equals(other: Any?): Boolean {
		val otherModifier = other as? MinimumTouchTargetModifier ?: return false
		return size == otherModifier.size
	}
	
	override fun hashCode(): Int {
		return size.hashCode()
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Suppress("ModifierInspectorInfo")
internal fun Modifier.minimumTouchTargetSize(): Modifier = composed {
	if (LocalMinimumTouchTargetEnforcement.current) {
		val size = LocalViewConfiguration.current.minimumTouchTargetSize
		MinimumTouchTargetModifier(size)
	} else {
		Modifier
	}
}

private const val BoxOutDuration = 100
private const val CheckAnimationDuration = 100

private val CheckboxRippleRadius = 24.dp
private val CheckboxDefaultPadding = 2.dp
private val CheckboxSize = 20.dp
private val RadiusSize = 2.dp
