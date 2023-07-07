package com.punedelights.user.adapter

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.punedelights.user.R
import com.punedelights.user.activity.MainActivity
import com.punedelights.user.adapter.SectionAdapter.SectionHolder
import com.punedelights.user.fragment.ProductListFragment
import com.punedelights.user.helper.Constant
import com.punedelights.user.model.OfferImages
import com.punedelights.user.model.Section

class SectionAdapter(
    val activity: Activity,
    private val sectionList: ArrayList<Section>,
) : RecyclerView.Adapter<SectionHolder>() {
    var hashMap: HashMap<String, Long> = HashMap()

    override fun getItemCount(): Int {
        return sectionList.size
    }

    override fun onBindViewHolder(holder: SectionHolder, position: Int) {
        try {
            val section = sectionList[position]
            if (section.products.size > 0) {
                val jsonArrayImages: ArrayList<OfferImages?> = section.offer_images

                val images = java.util.ArrayList<OfferImages?>()
                for (i in 0 until jsonArrayImages.size) {
                    try {
                        images.addAll(jsonArrayImages)
                        if (images.size > 0) {
                            holder.lytBelowSectionOfferImages.visibility = View.VISIBLE
                            holder.lytBelowSectionOfferImages.adapter =
                                OfferAdapter(activity, images)
                        } else {
                            holder.lytBelowSectionOfferImages.visibility = View.GONE
                        }


                    } catch (e: Exception) {
                        images.clear()
                        holder.lytBelowSectionOfferImages.visibility = View.GONE
                    }
                }

                holder.tvTitle.text = section.title
                holder.tvSubTitle.text = section.short_description
                holder.lytBelowSectionOfferImages.layoutManager = LinearLayoutManager(activity)
                holder.lytBelowSectionOfferImages.isNestedScrollingEnabled = false

                for (product in section.products) {
                    for (variant in product!!.variants) {
                        val unitMeasurement = if (variant.measurement_unit_name.equals(
                                "kg",
                                ignoreCase = true
                            ) || variant.measurement_unit_name.equals("ltr", ignoreCase = true)
                        ) 1000 else 1.toLong()
                        val unit = variant.measurement.toDouble().toLong() * unitMeasurement
                        if (!hashMap.containsKey(variant.product_id)) {
                            hashMap[variant.product_id] =
                                (variant.stock.toDouble() * (if (variant.stock_unit_name.equals(
                                        "kg",
                                        ignoreCase = true
                                    ) || variant.stock_unit_name.equals("ltr", ignoreCase = true)
                                ) 1000 else 1) - unit * variant.cart_count.toLong()).toLong()
                        } else {
                            hashMap.replace(
                                variant.product_id,
                                hashMap[variant.product_id]?.minus(unit * variant.cart_count.toLong())!!
                            )
                        }
                    }
                }
                when (section.style) {
                    "style_1" -> {
                        holder.recyclerView.layoutManager =
                            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                        val adapter =
                            AdapterStyle1(
                                activity,
                                section.products,
                                R.layout.offer_layout,
                                hashMap
                            )
                        holder.recyclerView.adapter = adapter
                    }

                    "style_2" -> {
                        holder.recyclerView.layoutManager = LinearLayoutManager(activity)
                        val adapterStyle2 = AdapterStyle2(activity, section.products, hashMap)
                        holder.recyclerView.adapter = adapterStyle2
                    }

                    "style_3" -> {
                        holder.recyclerView.layoutManager = GridLayoutManager(activity, 2)
                        val adapter3 =
                            AdapterStyle1(activity, section.products, R.layout.lyt_style_3, hashMap)
                        holder.recyclerView.adapter = adapter3
                    }
                }
                holder.tvMore.setOnClickListener {
                    val fragment: Fragment = ProductListFragment()
                    val bundle = Bundle()
                    bundle.putString(Constant.FROM, "section")
                    bundle.putString(Constant.NAME, section.title)
                    bundle.putString(Constant.ID, section.id)
                    fragment.arguments = bundle
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                        .addToBackStack(null).commit()
                }
            }else{
                holder.linearLayout.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.section_layout, parent, false)
        return SectionHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class SectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val tvMore: TextView = itemView.findViewById(R.id.tvMore)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        val lytBelowSectionOfferImages: RecyclerView =
            itemView.findViewById(R.id.lytBelowSectionOfferImages)

    }
}