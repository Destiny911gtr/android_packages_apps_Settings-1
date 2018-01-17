/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.security.trustagent;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.TestConfig;
import com.android.settings.testutils.FakeFeatureFactory;
import com.android.settings.testutils.SettingsRobolectricTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class ManageTrustAgentsPreferenceControllerTest {

    @Mock
    private TrustAgentManager mTrustAgentManager;
    @Mock
    private LockPatternUtils mLockPatternUtils;
    @Mock
    private PreferenceScreen mScreen;

    private FakeFeatureFactory mFeatureFactory;
    private Context mContext;
    private ManageTrustAgentsPreferenceController mController;
    private Preference mPreference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = RuntimeEnvironment.application;
        mFeatureFactory = FakeFeatureFactory.setupForTest();
        when(mFeatureFactory.securityFeatureProvider.getLockPatternUtils(mContext))
                .thenReturn(mLockPatternUtils);
        when(mFeatureFactory.securityFeatureProvider.getTrustAgentManager())
                .thenReturn(mTrustAgentManager);
        mController = new ManageTrustAgentsPreferenceController(mContext);
        mPreference = new Preference(mContext);
        mPreference.setKey(mController.getPreferenceKey());
        when(mScreen.findPreference(mController.getPreferenceKey()))
                .thenReturn(mPreference);
    }

    @Test
    public void isAlwaysAvailable() {
        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void displayPreference_isNotSecure_shouldDisablePreference() {
        when(mLockPatternUtils.isSecure(anyInt())).thenReturn(false);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isEnabled()).isFalse();
        assertThat(mPreference.getSummary())
                .isEqualTo(mContext.getString(R.string.disabled_because_no_backup_security));
    }

    @Test
    public void displayPreference_isSecure_noTrustAgent_shouldShowGenericSummary() {
        when(mLockPatternUtils.isSecure(anyInt())).thenReturn(true);
        when(mTrustAgentManager.getActiveTrustAgents(mContext, mLockPatternUtils))
                .thenReturn(new ArrayList<>());

        mController.displayPreference(mScreen);

        assertThat(mPreference.isEnabled()).isTrue();
        assertThat(mPreference.getSummary())
                .isEqualTo(mContext.getString(R.string.manage_trust_agents_summary));
    }

    @Test
    public void displayPreference_isSecure_hasTrustAgent_shouldShowDetailedSummary() {
        when(mLockPatternUtils.isSecure(anyInt())).thenReturn(true);
        when(mTrustAgentManager.getActiveTrustAgents(mContext, mLockPatternUtils))
                .thenReturn(Arrays.asList(new TrustAgentManager.TrustAgentComponentInfo()));

        mController.displayPreference(mScreen);

        assertThat(mPreference.isEnabled()).isTrue();
        assertThat(mPreference.getSummary())
                .isEqualTo(mContext.getResources().getQuantityString(
                        R.plurals.manage_trust_agents_summary_on, 1, 1));
    }
}