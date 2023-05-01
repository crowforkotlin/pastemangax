@file:SuppressLint("SetTextI18n")

package com.crow.module_main.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.current_project.getSpannableString
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_main.databinding.MainUpdateUrlRvBinding
import com.crow.module_main.model.resp.update.Url

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/ui/adapter
 * @Time: 2023/4/7 18:09
 * @Author: CrowForKotlin
 * @Description: MainAppUpdateRv
 * @formatter:on
 **************************/
class MainAppUpdateRv(val mUrl: List<Url>) : RecyclerView.Adapter<MainAppUpdateRv.ViewHolder>() {

    inner class ViewHolder(val rvBinding: MainUpdateUrlRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    private val mPurple = ContextCompat.getColor(appContext, R.color.base_purple_8f6af1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MainUpdateUrlRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->
            vh.rvBinding.mainUpdateUrlContent.doOnClickInterval {
                val intent = Intent()
                intent.data = Uri.parse(mUrl[vh.absoluteAdapterPosition].mLink)
                intent.action = Intent.ACTION_VIEW
                parent.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = mUrl.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rvBinding.mainUpdateUrlContent.text = mUrl[position].mName.getSpannableString(mPurple, 0)
    }
}