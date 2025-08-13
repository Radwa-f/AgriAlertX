package ma.ensaj.agri_alert.view.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.BackendImageResponse
import ma.ensaj.agri_alert.model.Crop
import ma.ensaj.agri_alert.network.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CropsAdapter(
    private val crops: List<Crop>,
    private val context: Context
) : RecyclerView.Adapter<CropsAdapter.CropViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crop, parent, false)
        return CropViewHolder(view)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        val crop = crops[position]
        holder.bind(crop, context)

        val query = crop.name

        // Fetch image for the crop using Unsplash API
        RetrofitClient.imagesApi.getCropImage(crop.name)
            .enqueue(object : Callback<BackendImageResponse> {
                override fun onResponse(
                    call: Call<BackendImageResponse>,
                    response: Response<BackendImageResponse>
                ) {
                    val imageUrl = response.body()?.url
                    Glide.with(holder.itemView.context)
                        .load(imageUrl ?: R.drawable.ic_corn)
                        .placeholder(R.drawable.ic_corn)
                        .into(holder.cropImageView)
                }
                override fun onFailure(call: Call<BackendImageResponse>, t: Throwable) {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.ic_crops)
                        .into(holder.cropImageView)
                }
            })

    }

    override fun getItemCount(): Int = crops.size

    class CropViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cropImageView: ImageView = itemView.findViewById(R.id.iv_crop_image)
        private val cropNameTextView: TextView = itemView.findViewById(R.id.tv_crop_name)
        private val cropHarvestTextView: TextView = itemView.findViewById(R.id.tv_crop_status)

        fun bind(crop: Crop, context: Context) {
            cropNameTextView.text = crop.name

            // Get harvest time from strings.xml
            val cropNames = context.resources.getStringArray(R.array.crop_names)
            val harvestTimes = context.resources.getStringArray(R.array.crop_harvest_times)
            val harvestTime = harvestTimes[cropNames.indexOf(crop.name)]

            cropHarvestTextView.text = harvestTime
        }
    }
}
