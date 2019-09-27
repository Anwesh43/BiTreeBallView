package com.anwesh.uiprojects.bitreeballpropagationview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.bitreeballview.BiTreeBallView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BiTreeBallView.create(this)
    }
}
