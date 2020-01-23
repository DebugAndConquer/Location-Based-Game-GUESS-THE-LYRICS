package com.example.guessthelyrics

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*

class CustomAdapter(context: Context) : BaseAdapter() {
    // A set of headers used to separate songs from each other
    private val header = TreeSet<Int>()
    // Song lyric data
    private val data = ArrayList<String>()
    private val inf: LayoutInflater = context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * Adds a lyric to a view
     */
    fun createItem(item: String) {
        data.add(item)
        notifyDataSetChanged()
    }

    /**
     * Adds a header to a view
     */
    fun createHeader(item: String) {
        data.add(item)
        header.add(data.size - 1)
        notifyDataSetChanged()
    }

    /**
     * Returns the type of item. It is rather separator (header) or the item itself
     */
    override fun getItemViewType(position: Int): Int {
        return if (header.contains(position)) TYPE_SEPARATOR else TYPE_ITEM
    }

    /**
     * Returns 2 because there is only 2 types of items in this adapter
     */
    override fun getViewTypeCount(): Int {
        return 2
    }

    /**
     * Returns the number of pieces of data
     */
    override fun getCount(): Int {
        return data.size
    }

    /**
     * Returns the item itself
     */
    override fun getItem(pos: Int): String {
        return data[pos]
    }

    /**
     * Returns the id/position of an item
     */
    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    /**
     * Returns a view produced by this adapter
     */
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        var convView = convertView
        val vHolder: ViewHolder?
        val rowType = getItemViewType(pos)

        if (convView == null) {
            vHolder = ViewHolder()
            when (rowType) {
                0 -> {
                    convView = with(inf) { inflate(R.layout.text, null) }
                    vHolder.textView = convView!!.findViewById(R.id.text) as TextView
                }
                1 -> {
                    convView = with(inf) { inflate(R.layout.text_separator, null) }
                    vHolder.textView = convView!!.findViewById(R.id.textSeparator) as TextView
                }
            }
            convView!!.tag = vHolder
        } else {
            vHolder = convView.tag as ViewHolder
        }
        vHolder.textView!!.text = data[pos]

        return convView
    }

    class ViewHolder {
        var textView: TextView? = null
    }

    // Enumerated types of data stored
    companion object {

        private const val TYPE_ITEM = 0
        private const val TYPE_SEPARATOR = 1
    }

}