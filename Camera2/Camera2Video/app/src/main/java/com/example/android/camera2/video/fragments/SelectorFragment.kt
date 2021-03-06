/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2.video.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.camera.utils.GenericListAdapter
import com.example.android.camera2.video.R
import kotlinx.android.synthetic.main.fragment_selector.view.*

/**
 * In this [Fragment] we let users pick a camera, size and FPS to use for high
 * speed video recording
 */
class SelectorFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ):View? = inflater.inflate(R.layout.fragment_selector, container, false)

    @SuppressLint("MissingPermission")
    //Called immediately after onCreateView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("DEBUG", "Created apps !")

        // System service for detecting, characterizing and connecting to CameraDevice
        val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // cameraList : list of every combination of resolution and FPS
        val cameraList = enumerateVideoCameras(cameraManager)


        view.btnCamera1.text=cameraList[0].name
        view.btnCamera2.text = cameraList[1].name

        view.btnCamera1.setOnClickListener{
            // Send camera and flash info to the next fragment
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(SelectorFragmentDirections.actionSelectorToCamera(
                            cameraList[0].cameraId, cameraList[0].size.width, cameraList[0].size.height, cameraList[0].fps, mFlashSupported))
        }


        view.btnCamera2.setOnClickListener {
            // Send camera and flash info to the next fragment
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(SelectorFragmentDirections.actionSelectorToCamera(
                            cameraList[1].cameraId, cameraList[1].size.width, cameraList[1].size.height, cameraList[1].fps, mFlashSupported))
        }


       /* view as RecyclerView
        view.apply {
            layoutManager = LinearLayoutManager(requireContext())

            // System service for detecting, characterizing and connecting to CameraDevice
            val cameraManager =
                    requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

            // cameraList : list of every combination of resolution and FPS
            val cameraList = enumerateVideoCameras(cameraManager)

            val layoutId = android.R.layout.simple_list_item_1
            adapter = GenericListAdapter(cameraList, itemLayoutId = layoutId) { view, item, _ ->
                view.findViewById<TextView>(android.R.id.text1).text = item.name
                view.setOnClickListener {
                    // Send camera and flash info to the next fragment
                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
                            .navigate(SelectorFragmentDirections.actionSelectorToCamera(
                                    item.cameraId, item.size.width, item.size.height, item.fps, mFlashSupported))

                }
            }
        }*/
    }

    companion object {

        var mFlashSupported = false

        private data class CameraInfo(
                val name: String,
                val cameraId: String,
                val size: Size,
                val fps: Int)

        /** Converts a lens orientation enum into a human-readable string */
        private fun lensOrientationString(value: Int) = when (value) {
            CameraCharacteristics.LENS_FACING_BACK -> "Back"
            CameraCharacteristics.LENS_FACING_FRONT -> "Front"
            CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
            else -> "Unknown"
        }

        /** Lists all video-capable cameras and supported resolution and FPS combinations */
        @SuppressLint("InlinedApi")
        private fun enumerateVideoCameras(cameraManager: CameraManager): List<CameraInfo> {
            val availableCameras: MutableList<CameraInfo> = mutableListOf()

            // Iterate over the list of cameras and add those with high speed video recording
            //  capability to our output. This function only returns those cameras that declare
            //  constrained high speed video recording, but some cameras may be capable of doing
            //  unconstrained video recording with high enough FPS for some use cases and they will
            //  not necessarily declare constrained high speed video capability.
            cameraManager.cameraIdList.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)

                val facing = characteristics.get(CameraCharacteristics.LENS_FACING) as Int

                val orientation = lensOrientationString(
                        characteristics.get(CameraCharacteristics.LENS_FACING)!!)

                // Query the available capabilities and output formats
                val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!
                val cameraConfig = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

                // Check flash availability for back camera
                if(facing != null && facing == CameraCharacteristics.LENS_FACING_BACK){
                    val availability = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) as Boolean
                    mFlashSupported = if ((availability == null)) false else availability
                }

                // Return cameras that declare to be backward compatible
                if (capabilities.contains(CameraCharacteristics
                                .REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)) {
                    // Recording should always be done in the most efficient format, which is
                    //  the format native to the camera framework
                    val targetClass = MediaRecorder::class.java

                    // For each size, list the expected FPS
                    cameraConfig.getOutputSizes(targetClass).forEach { size ->
                        // Get the number of seconds that each frame will take to process
                        val secondsPerFrame =
                                cameraConfig.getOutputMinFrameDuration(targetClass, size) /
                                        1_000_000_000.0
                        // Compute the frames per second to let user select a configuration
                        val fps = if (secondsPerFrame > 0) (1.0 / secondsPerFrame).toInt() else 0
                        val fpsLabel = if (fps > 0) "$fps" else "N/A"
                        if(facing != null && facing == CameraCharacteristics.LENS_FACING_BACK){
                            if( ( size.height == 1080 && size.width==1920) || (size.height==720 && size.width==1280))
                            availableCameras.add(CameraInfo(
                                    "$orientation ($id) $size $fpsLabel FPS", id, size, fps))
                        }
                    }
                }
            }

            return availableCameras
        }
    }
}
