package contacts.sample.util

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView

interface AbstractMultiChoiceModeListener : AbsListView.MultiChoiceModeListener {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false

    override fun onDestroyActionMode(mode: ActionMode) {}

    override fun onItemCheckedStateChanged(
        mode: ActionMode,
        position: Int,
        id: Long,
        checked: Boolean
    ) {
    }
}