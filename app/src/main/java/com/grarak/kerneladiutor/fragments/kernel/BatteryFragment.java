/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.fragments.kernel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.fragments.DescriptionFragment;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.battery.Battery;
import com.grarak.kerneladiutor.views.recyclerview.CardView;
import com.grarak.kerneladiutor.views.recyclerview.SelectView;
import com.grarak.kerneladiutor.views.recyclerview.RecyclerViewItem;
import com.grarak.kerneladiutor.views.recyclerview.SeekBarView;
import com.grarak.kerneladiutor.views.recyclerview.StatsView;
import com.grarak.kerneladiutor.views.recyclerview.SwitchView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 26.06.16.
 */
public class BatteryFragment extends RecyclerViewFragment {

    private StatsView mLevel;
    private StatsView mVoltage;
    private StatsView mCurrent;

    private static int sBatteryLevel;
    private static int sBatteryVoltage;
    private static int sBatteryCurrent;

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        levelInit(items);
        voltageInit(items);
        currentInit(items);
        if (Battery.hasForceFastCharge()) {
            forceFastChargeInit(items);
        }
        if (Battery.hasBlx()) {
            blxInit(items);
        }
        if (Battery.hasQCCurrent()) {
	    quickChargeInit(items);
	}
        chargeRateInit(items);
    }

    @Override
    protected void postInit() {
        super.postInit();

        if (itemsSize() > 2) {
            addViewPagerFragment(ApplyOnBootFragment.newInstance(this));
        }
        if (Battery.hasCapacity(getActivity())) {
            addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.capacity),
                    Battery.getCapacity(getActivity()) + getString(R.string.mah)));
        }
    }

    private void levelInit(List<RecyclerViewItem> items) {
        mLevel = new StatsView();
        mLevel.setTitle(getString(R.string.level));

        items.add(mLevel);
    }

    private void voltageInit(List<RecyclerViewItem> items) {
        mVoltage = new StatsView();
        mVoltage.setTitle(getString(R.string.voltage));

        items.add(mVoltage);
    }

    private void currentInit(List<RecyclerViewItem> items) {
        mCurrent = new StatsView();
        mCurrent.setTitle(getString(R.string.current));

        items.add(mCurrent);
    }

    private void forceFastChargeInit(List<RecyclerViewItem> items) {
        SwitchView forceFastCharge = new SwitchView();
        forceFastCharge.setTitle(getString(R.string.usb_fast_charge));
        forceFastCharge.setSummary(getString(R.string.usb_fast_charge_summary));
        forceFastCharge.setChecked(Battery.isForceFastChargeEnabled());
        forceFastCharge.addOnSwitchListener(new SwitchView.OnSwitchListener() {
            @Override
            public void onChanged(SwitchView switchView, boolean isChecked) {
                Battery.enableForceFastCharge(isChecked, getActivity());
            }
        });

        items.add(forceFastCharge);
    }

    private void blxInit(List<RecyclerViewItem> items) {
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.disabled));
        for (int i = 0; i <= 100; i++) {
            list.add(String.valueOf(i));
        }

        SeekBarView blx = new SeekBarView();
        blx.setTitle(getString(R.string.blx));
        blx.setSummary(getString(R.string.blx_summary));
        blx.setItems(list);
        blx.setProgress(Battery.getBlx());
        blx.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
            @Override
            public void onStop(SeekBarView seekBarView, int position, String value) {
                Battery.setBlx(position, getActivity());
            }

            @Override
            public void onMove(SeekBarView seekBarView, int position, String value) {
            }
        });

        items.add(blx);
    }

    private void chargeRateInit(List<RecyclerViewItem> items) {
        CardView chargeRateCard = new CardView(getActivity());
        chargeRateCard.setTitle(getString(R.string.charge_rate));

        if (Battery.hasChargeRateEnable()) {
            SwitchView chargeRate = new SwitchView();
            chargeRate.setSummary(getString(R.string.charge_rate));
            chargeRate.setChecked(Battery.isChargeRateEnabled());
            chargeRate.addOnSwitchListener(new SwitchView.OnSwitchListener() {
                @Override
                public void onChanged(SwitchView switchView, boolean isChecked) {
                    Battery.enableChargeRate(isChecked, getActivity());
                }
            });

            chargeRateCard.addItem(chargeRate);
        }

        if (Battery.hasChargingCurrent()) {
            SeekBarView chargingCurrent = new SeekBarView();
            chargingCurrent.setTitle(getString(R.string.charging_current));
            chargingCurrent.setSummary(getString(R.string.charging_current_summary));
            chargingCurrent.setUnit(getString(R.string.ma));
            chargingCurrent.setMax(1500);
            chargingCurrent.setMin(100);
            chargingCurrent.setOffset(10);
            chargingCurrent.setProgress(Battery.getChargingCurrent() / 10 - 10);
            chargingCurrent.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                @Override
                public void onStop(SeekBarView seekBarView, int position, String value) {
                    Battery.setChargingCurrent((position + 10) * 10, getActivity());
                }

                @Override
                public void onMove(SeekBarView seekBarView, int position, String value) {
                }
            });

            chargeRateCard.addItem(chargingCurrent);
        }

        if (chargeRateCard.size() > 0) {
            items.add(chargeRateCard);
        }
    }

     private void quickChargeInit(List<RecyclerViewItem> items) {
         CardView quickChargeCard = new CardView(getActivity());
         quickChargeCard.setTitle(getString(R.string.quick_charge));

         if (Battery.hasQuickChargeEnable()) {
             SwitchView quickCharge = new SwitchView();
             quickCharge.setSummary(getString(R.string.quick_charge));
             quickCharge.setChecked(Battery.isQuickChargeEnabled());
             quickCharge.addOnSwitchListener(new SwitchView.OnSwitchListener() {
                 @Override
                 public void onChanged(SwitchView switchView, boolean isChecked) {
                     Battery.enableQuickCharge(isChecked, getActivity());
		}
	});

             quickChargeCard.addItem(quickCharge);
	}

        if (Battery.hasChargeProfile()) {
			List<String> freqs = new ArrayList<>();
            SelectView chargeProfile = new SelectView();
            chargeProfile.setTitle(getString(R.string.charge_profile));
	    chargeProfile.setSummary(getString(R.string.charge_profile_summary));
	    chargeProfile.setItems(Battery.getProfilesMenu(getActivity()));
	    chargeProfile.setItem(Battery.getProfiles());
	    chargeProfile.setOnItemSelected(new SelectView.OnItemSelected() {
		@Override
		public void onItemSelected(SelectView selectView, int position, String item) {
                    Battery.setchargeProfile(position, getActivity());
			}
		});

             quickChargeCard.addItem(chargeProfile);
         }

         if (Battery.hasQCCurrent()) {
             SeekBarView quickCurrent = new SeekBarView();
             quickCurrent.setTitle(getString(R.string.charging_current));
             quickCurrent.setSummary(getString(R.string.charging_current_summary));
             quickCurrent.setUnit(getString(R.string.ma));
             quickCurrent.setMax(1500);
             quickCurrent.setMin(100);
             quickCurrent.setOffset(10);
             quickCurrent.setProgress(Battery.getQCCurrent() / 10 - 10);
             quickCurrent.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                 @Override
                 public void onStop(SeekBarView seekBarView, int position, String value) {
                     Battery.setQCCurrent((position + 10) * 10, getActivity());
		}

                 @Override
                 public void onMove(SeekBarView seekBarView, int position, String value) {
		}
	});

             quickChargeCard.addItem(quickCurrent);
	}

         if (quickChargeCard.size() > 0) {
             items.add(quickChargeCard);
		}
	};

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            sBatteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            sBatteryCurrent = Integer.valueOf(Battery.getCurrentNow());
        }
    };

    @Override
    protected void refresh() {
        super.refresh();
        if (mLevel != null) {
            mLevel.setStat(sBatteryLevel + "%");
        }
        if (mVoltage != null) {
            mVoltage.setStat(sBatteryVoltage + getString(R.string.mv));
        }
        if (mCurrent != null) {
            mCurrent.setStat(sBatteryCurrent + getString(R.string.ma));
	}
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(mBatteryReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

}
