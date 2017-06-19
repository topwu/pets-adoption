package com.topwu.petsadopt

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.topwu.petsadopt.model.pet.Pet
import com.topwu.petsadopt.util.TransitionUtils
import kotlinx.android.synthetic.main.item_pet.view.*

class BaliPlacesAdapter constructor(private val listener: OnPlaceClickListener,
                                    private val context: Context) : RecyclerView.Adapter<BaliPlacesAdapter.BaliViewHolder>() {

    private val petList = ArrayList<Pet>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaliViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_pet, parent, false)
        return BaliViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaliViewHolder, position: Int) {
        val pet = petList[position]

        holder.place.text = pet.shelter.name
        holder.age.text = pet.age

        if (pet.remark != null) {
            holder.remark.text = pet.remark
        }

        if (pet.album.file.isNotEmpty()) {
            holder.photo.setImageURI(pet.album.file)
        }

        holder.root.setOnClickListener({ _ ->
            listener.onPlaceClicked(holder.root, TransitionUtils.getRecyclerViewTransitionName(position), position)
        })
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    fun getPetList(): List<Pet> = petList

    fun setPetList(pets: List<Pet>) {
        petList.clear()
        petList.addAll(pets)
        notifyDataSetChanged()
    }

    interface OnPlaceClickListener {
        fun onPlaceClicked(sharedView: View, transitionName: String, position: Int)
    }

    class BaliViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: SimpleDraweeView = itemView.photo
        val place: TextView = itemView.place
        val age: TextView = itemView.age
        val remark: TextView = itemView.remark
        val root: CardView = itemView.root
    }
}