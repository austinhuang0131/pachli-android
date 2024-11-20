/*
 * Copyright 2024 Pachli Association
 *
 * This file is a part of Pachli.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Pachli is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Pachli; if not,
 * see <http://www.gnu.org/licenses>.
 */

package app.pachli.components.trending

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.recyclerview.widget.RecyclerView
import app.pachli.R
import app.pachli.core.ui.accessibility.PachliRecyclerViewAccessibilityDelegate
import app.pachli.view.PreviewCardView
import app.pachli.view.PreviewCardView.Target

/**
 * Accessbility delete for [TrendingLinkViewHolder].
 *
 * Each item shows an action to open the link.
 *
 * If present, an item to show the author's account is also included.
 */
internal class TrendingLinksAccessibilityDelegate(
    private val recyclerView: RecyclerView,
    val listener: PreviewCardView.OnClickListener,
) : PachliRecyclerViewAccessibilityDelegate(recyclerView) {
    private val openLinkAction = AccessibilityActionCompat(
        app.pachli.core.ui.R.id.action_open_link,
        context.getString(R.string.action_open_link),
    )

    private val copyLinkAction = AccessibilityActionCompat(
        app.pachli.core.ui.R.id.action_copy_item,
        context.getString(R.string.action_copy_link),
    )

    private val openBylineAccountAction = AccessibilityActionCompat(
        app.pachli.core.ui.R.id.action_open_byline_account,
        context.getString(R.string.action_open_byline_account),
    )

    private val delegate = object : ItemDelegate(this) {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)

            val viewHolder = recyclerView.findContainingViewHolder(host)
                as TrendingLinkViewHolder

            info.addAction(openLinkAction)
            info.addAction(copyLinkAction)

            viewHolder.link.authors?.firstOrNull()?.account?.let {
                info.addAction(openBylineAccountAction)
            }
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
            val viewHolder = recyclerView.findContainingViewHolder(host)
                as TrendingLinkViewHolder

            return when (action) {
                app.pachli.core.ui.R.id.action_open_link -> {
                    interrupt()
                    listener.onClick(viewHolder.link, Target.CARD)
                    true
                }
                app.pachli.core.ui.R.id.action_copy_item -> {
                    val clipboard = ContextCompat.getSystemService(
                        context,
                        ClipboardManager::class.java,
                    ) as ClipboardManager
                    val clip = ClipData.newPlainText("", viewHolder.link.url)
                    clipboard.setPrimaryClip(clip)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        Toast.makeText(
                            context,
                            context.getString(app.pachli.core.ui.R.string.item_copied),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    true
                }
                app.pachli.core.ui.R.id.action_open_byline_account -> {
                    interrupt()
                    listener.onClick(viewHolder.link, Target.BYLINE)
                    true
                }
                else -> super.performAccessibilityAction(host, action, args)
            }
        }
    }

    override fun getItemDelegate(): AccessibilityDelegateCompat = delegate
}
