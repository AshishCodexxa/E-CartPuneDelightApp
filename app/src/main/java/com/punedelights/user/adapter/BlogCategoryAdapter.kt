package com.punedelights.user.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.punedelights.user.R
import com.punedelights.user.activity.MainActivity
import com.punedelights.user.fragment.BlogListFragment
import com.punedelights.user.model.BlogCategory

class BlogCategoryAdapter(val activity: Activity, private val blogCategories: ArrayList<BlogCategory?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    var visible = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(LayoutInflater.from(activity).inflate(R.layout.lyt_category_blog_list, parent, false))

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val blogCategory = blogCategories[position]!!
            holderParent.tvTitle.text = blogCategory.name
            Glide.with(activity).load(blogCategory.image)
                
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holderParent.imgBlogCategory)
            holderParent.lytMain.setOnClickListener {
                val fragment: Fragment = BlogListFragment()
                val bundle = Bundle()
                bundle.putString("title", blogCategory.name)
                bundle.putString("id", blogCategory.id)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return blogCategories.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        return blogCategories[position]?.id.toString().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgBlogCategory: ImageView = itemView.findViewById(R.id.imgBlogCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val lytMain: LinearLayout = itemView.findViewById(R.id.lytMain)

    }
}