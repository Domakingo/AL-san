package com.doma.alsan.helper.extensions

import android.content.Context
import android.util.TypedValue
import com.doma.alsan.R

fun Context.getAttrValue(attrResId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrResId, typedValue, true)
    return typedValue.data
}

fun Context.getThemePrimaryColor(): Int {
    return getAttrValue(R.attr.themePrimaryColor)
}

fun Context.getThemeSecondaryColor(): Int {
    return getAttrValue(R.attr.themeSecondaryColor)
}

fun Context.getThemeTextColor(): Int {
    return getAttrValue(R.attr.themeContentColor)
}

fun Context.getThemeCardColor(): Int {
    return getAttrValue(R.attr.themeCardColor)
}

fun Context.getThemeToolbarColor(): Int {
    return getAttrValue(R.attr.themeCardColor)
}

fun Context.getThemeBackgroundColor(): Int {
    return getAttrValue(R.attr.themeBackgroundColor)
}

fun Context.getThemeFloatingButtonColor(): Int {
    return getAttrValue(R.attr.themeSecondaryColor)
}

fun Context.getThemeFloatingIconColor(): Int {
    return getAttrValue(R.attr.themeBackgroundColor)
}

fun Context.getThemeNegativeColor(): Int {
    return getAttrValue(R.attr.themeNegativeColor)
}