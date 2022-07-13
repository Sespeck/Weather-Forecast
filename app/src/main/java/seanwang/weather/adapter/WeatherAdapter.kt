package seanwang.weather.adapter

import seanwang.weather.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import seanwang.weather.model.Weather
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class WeatherAdapter(private val context: Context, private val dataset: List<Weather>)
    :RecyclerView.Adapter<WeatherAdapter.ItemViewHolder>()
{

    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val windspeed:TextView = view.findViewById(R.id.card_windspeed)
        val temperature:TextView = view.findViewById(R.id.card_temperature)
        val time:TextView = view.findViewById(R.id.card_time)
        val condition: ImageView = view.findViewById(R.id.card_condition)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout:View = LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    //This method is called by the layout manager in order to replace the contents of a list item view.
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        val picasso = Picasso.get()
        picasso.load("https:"+item.icon).into(holder.condition)
        holder.time.text = item.time
        holder.windspeed.text = item.windSpeed+"Km/h"
        holder.temperature.text = item.temperature+"°C"
        val input: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val output: SimpleDateFormat = SimpleDateFormat("hh: mm aa")
        try{
            val t:Date = input.parse(item.time)
            holder.time.text = output.format(t)
        }
        catch (e:ParseException){
            e.printStackTrace()
        }
    }

    override fun getItemCount() = dataset.size
}
