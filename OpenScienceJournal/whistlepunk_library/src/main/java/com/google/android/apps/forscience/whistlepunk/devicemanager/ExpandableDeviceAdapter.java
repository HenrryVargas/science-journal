/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.google.android.apps.forscience.whistlepunk.devicemanager;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.SensorAppearanceProvider;
import com.google.android.apps.forscience.whistlepunk.SensorRegistry;
import com.google.android.apps.forscience.whistlepunk.api.scalarinput.InputDeviceSpec;
import com.google.android.apps.forscience.whistlepunk.metadata.ExternalSensorSpec;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpandableDeviceAdapter extends
        CompositeSensitiveExpandableAdapter<DeviceParentViewHolder, SensorChildViewHolder>
        implements SensorGroup, CompositeRecyclerAdapter.CompositeSensitiveAdapter {
    private final List<DeviceParentListItem> mDeviceParents;
    private final DeviceRegistry mDeviceRegistry;
    private Map<String, ConnectableSensor> mSensorMap = new ArrayMap<>();
    private ConnectableSensorRegistry mRegistry;
    private final SensorAppearanceProvider mAppearanceProvider;
    private final SensorRegistry mSensorRegistry;

    public static ExpandableDeviceAdapter createEmpty(final ConnectableSensorRegistry registry,
            DeviceRegistry deviceRegistry, SensorAppearanceProvider appearanceProvider,
            SensorRegistry sensorRegistry, int uniqueId) {
        return new ExpandableDeviceAdapter(registry,
                new ArrayList<DeviceParentListItem>(), deviceRegistry, appearanceProvider,
                sensorRegistry, uniqueId);
    }

    private ExpandableDeviceAdapter(final ConnectableSensorRegistry registry,
            List<DeviceParentListItem> deviceParents, DeviceRegistry deviceRegistry,
            SensorAppearanceProvider appearanceProvider, SensorRegistry sensorRegistry,
            int uniqueId) {
        super(deviceParents, uniqueId);
        mRegistry = Preconditions.checkNotNull(registry);
        mDeviceParents = deviceParents;
        mDeviceRegistry = deviceRegistry;
        mAppearanceProvider = appearanceProvider;
        mSensorRegistry = sensorRegistry;
    }

    @Override
    public DeviceParentViewHolder onCreateParentViewHolder(
            ViewGroup parentViewGroup) {
        View viewGroup = LayoutInflater.from(parentViewGroup.getContext()).inflate(
                R.layout.device_expandable_recycler_item, parentViewGroup, false);
        return new DeviceParentViewHolder(viewGroup, offsetSupplier(),
                new DeviceParentViewHolder.MenuCallbacks() {
                    @Override
                    public void forgetDevice(InputDeviceSpec spec) {
                        mRegistry.forgetMyDevice(spec, mSensorRegistry);
                    }
                });
    }

    @Override
    public void onBindParentViewHolder(DeviceParentViewHolder parentViewHolder, int position,
            ParentListItem parentListItem) {
        parentViewHolder.bind((DeviceParentListItem) parentListItem, mSensorMap);
    }

    @Override
    public SensorChildViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View viewGroup = LayoutInflater.from(childViewGroup.getContext()).inflate(
                R.layout.sensor_child_recycler_item, childViewGroup, false);
        return new SensorChildViewHolder(viewGroup, mAppearanceProvider);
    }

    @Override
    public void onBindChildViewHolder(SensorChildViewHolder childViewHolder,
            int position, Object childListItem) {
        childViewHolder.bind((String) childListItem, mSensorMap, mRegistry, mSensorRegistry);
    }

    @Override
    public boolean hasSensorKey(String sensorKey) {
        return mSensorMap.containsKey(sensorKey);
    }

    @Override
    public boolean addAvailableSensor(String sensorKey, ConnectableSensor sensor) {
        // TODO: can we reduce the duplication with addSensor?
        boolean isReplacement = mSensorMap.containsKey(sensorKey);
        if (isReplacement) {
            mSensorMap.put(sensorKey, sensor);
            notifyChildItemChanged(findParentIndex(sensorKey), findChildIndex(sensorKey));
            return true;
        }
        ExternalSensorSpec spec = sensor.getSpec();

        // Do we already have an item for this device?  If so, add the sensor there.
        InputDeviceSpec device = mDeviceRegistry.getDevice(spec);
        int i = findDevice(device);
        if (i >= 0) {
            mSensorMap.put(sensorKey, sensor);
            addSensorToDevice(i, sensorKey);
            return true;
        }

        return false;
    }

    private void addSensorToDevice(int deviceIndex, String sensorKey) {
        DeviceParentListItem parent = mDeviceParents.get(deviceIndex);
        parent.addSensor(sensorKey);
        notifyChildItemInserted(deviceIndex, parent.getChildItemList().size() - 1);
    }

    @Override
    public void addSensor(String sensorKey, ConnectableSensor sensor) {
        boolean addedToMyDevice = addAvailableSensor(sensorKey, sensor);
        mSensorMap.put(sensorKey, sensor);
        if (!addedToMyDevice) {
            // Otherwise, add a new device item.
            InputDeviceSpec device = mDeviceRegistry.getDevice(sensor.getSpec());
            DeviceParentListItem item = new DeviceParentListItem(device, mAppearanceProvider);
            item.addSensor(sensorKey);
            addDevice(item);
        }
    }

    private int findDevice(InputDeviceSpec device) {
        for (int i = 0; i < mDeviceParents.size(); i++) {
            DeviceParentListItem parent = mDeviceParents.get(i);
            if (parent.isDevice(device)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addAvailableDevice(ExternalSensorDiscoverer.DiscoveredDevice device) {
        // Don't need anything here; we'll grab from DeviceRegistry if this is My Device
    }

    @Override
    public void setMyDevices(List<InputDeviceSpec> myDevices) {
        List<InputDeviceSpec> unaccountedDevices = new ArrayList<>(myDevices);

        for (int i = mDeviceParents.size() - 1; i >= 0; i--) {
            removeUnlessPresent(i, unaccountedDevices);
        }

        for (InputDeviceSpec unaccountedDevice : unaccountedDevices) {
            addDevice(new DeviceParentListItem(unaccountedDevice, mAppearanceProvider));
        }
    }

    private void removeUnlessPresent(int i, List<InputDeviceSpec> unaccountedDevices) {
        DeviceParentListItem parent = mDeviceParents.get(i);
        for (int j = unaccountedDevices.size() - 1; j >= 0; j--) {
            if (parent.isDevice(unaccountedDevices.get(j))) {
                unaccountedDevices.remove(j);
                return;
            }
        }
        mDeviceParents.remove(i);
        notifyParentItemRemoved(i);
    }

    private void addDevice(DeviceParentListItem item) {
        if (item.isPhoneSensorParent(mDeviceRegistry)) {
            // add phone sensor container always at top
            mDeviceParents.add(0, item);
            notifyParentItemInserted(0);
        } else {
            mDeviceParents.add(item);
            int parentPosition = mDeviceParents.size() - 1;
            notifyParentItemInserted(parentPosition);
        }
    }

    private int findParentIndex(String sensorKey) {
        for (int i = 0; i < mDeviceParents.size(); i++) {
            if (mDeviceParents.get(i).sensorIndexOf(sensorKey) > -1) {
                return i;
            }
        }
        return -1;
    }

    private int findChildIndex(String sensorKey) {
        for (int i = 0; i < mDeviceParents.size(); i++) {
            int sensorIndex = mDeviceParents.get(i).sensorIndexOf(sensorKey);
            if (sensorIndex > -1) {
                return sensorIndex;
            }
        }
        return -1;
    }

    @Override
    public boolean removeSensor(String sensorKey) {
        // We don't expect this to be called, since we only use Expandable for "My Devices", and
        // there we only enable/disable known sensors under a device, and "forget" devices

        // TODO: implement "forget my device"
        return false;
    }

    @Override
    public void replaceSensor(String sensorKey, ConnectableSensor sensor) {
        addSensor(sensorKey, sensor);
    }

    @Override
    public int getSensorCount() {
        return mSensorMap.size();
    }

    public void setProgress(boolean isScanning) {
        // TODO: update UI to show scan status
    }

    DeviceParentListItem getDevice(int position) {
        return mDeviceParents.get(position);
    }

    ConnectableSensor getSensor(int deviceIndex, int sensorIndex) {
        return mSensorMap.get(getDevice(deviceIndex).getSensorKey(sensorIndex));
    }

    @Override
    public void addAvailableService(ExternalSensorDiscoverer.DiscoveredService service) {
        // This view doesn't track services
    }
}