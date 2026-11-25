package com.android.ssaid.abdullah.core.hook

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class HookConfigProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf(COLUMN_SSAID))
        val ctx = context ?: return cursor
        val targetPackage = uri.lastPathSegment ?: return cursor
        val store = HookConfigStore(ctx)
        val overrideId = store.getOverride(targetPackage)
        if (!overrideId.isNullOrBlank()) {
            cursor.addRow(arrayOf(overrideId))
        }
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.item/vnd.$AUTHORITY.ssaid"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        const val AUTHORITY = "com.android.ssaid.abdullah.hookconfig"
        const val COLUMN_SSAID = "ssaid"
    }
}
