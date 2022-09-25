package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    Sensor firstSensor;
    Sensor secondSensor;
    @InjectMocks
    SecurityService securityService;
    @Mock
    SecurityRepository securityRepository;
    @Mock
    ImageService imageService;

    @BeforeEach
    void init() {
        firstSensor = new Sensor("firstSensor", SensorType.WINDOW);
        secondSensor = new Sensor("secondSensor", SensorType.DOOR);
        securityService.addSensor(firstSensor);
        securityService.addSensor(secondSensor);
    }

    /**
     * Test 1: If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
     */
    @Test
    void given_armed_when_sensor_activated_then_alarm_status_pending() {
        //given
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        //when
        securityService.changeSensorActivationStatus(firstSensor, true);
        //then
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    /**
     * Test 2: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
     */
    @Test
    void given_armed_and_alarm_pending_when_sensor_activated_then_alarm_status_alarm() {
        //given
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        //when
        securityService.changeSensorActivationStatus(firstSensor, true);
        //then
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * Test 3: If pending alarm and all sensors are inactive, return to no alarm state.
     */
    @Test
    void given_alarm_pending_when_all_sensors_inactive_then_alarm_status_no_alarm() {
        //given
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        firstSensor.setActive(true);
        secondSensor.setActive(true);
        //when
        securityService.changeSensorActivationStatus(firstSensor, false);
        securityService.changeSensorActivationStatus(secondSensor, false);
        //then
        verify(securityRepository, times(2)).setAlarmStatus(AlarmStatus.NO_ALARM); // 2 times because setAlarm is called for each sensor
    }

    /**
     * Test 4: If alarm is active, change in sensor state should not affect the alarm state.
     */
    @Test
    void given_alarm_status_alarm_when_some_sensor_inactive_then_alarm_status_alarm() {
        //given
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        firstSensor.setActive(true);
        secondSensor.setActive(true);
        //when
        securityService.changeSensorActivationStatus(firstSensor, false);
        //then
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    /**
     * Test 4: If alarm is active, change in sensor state should not affect the alarm state.
     */
    @Test
    void given_alarm_status_alarm_when_all_sensor_inactive_then_alarm_status_alarm() {
        //given
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        firstSensor.setActive(true);
        secondSensor.setActive(true);
        //when
        securityService.changeSensorActivationStatus(firstSensor, false);
        securityService.changeSensorActivationStatus(secondSensor, false);
        //then
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    /**
     * Test 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
     */
    @Test
    void given_sensor_active_when_sensor_active_again_and_alarm_status_pending_then_alarm_status_alarm() {
        //given
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        firstSensor.setActive(true);
        //when
        securityService.changeSensorActivationStatus(firstSensor, true);
        //then
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * Test 6: If a sensor is deactivated while already inactive, make no changes to the alarm state.
     */
    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void given_sensor_inactive_when_sensor_deactivated_then_alarm_status_not_changing(AlarmStatus alarmStatus) {
        //given
        when(securityService.getAlarmStatus()).thenReturn(alarmStatus);
        //when
        securityService.changeSensorActivationStatus(firstSensor, false); // sensor false per default
        //then
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    /**
     * Test 7: If the image service identifies an image containing a cat
     * while the system is armed-home, put the system into alarm status.
     */
    @Test
    void given_armed_home_when_imageservice_identify_cat_then_alarm_status_alarm() {
        //given
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        //when
        securityService.processImage(mock(BufferedImage.class));
        //then
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

    /**
     * Test 8: If the image service identifies an image that does not contain a cat, change the status to no alarm
     * as long as the sensors are not active.
     */

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void given_sensors_inactive_when_imageservice_does_not_detect_cat_then_alarm_status_no_alarm(AlarmStatus alarmStatus){
        //given
//        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(false);
        //when
        securityService.processImage(mock(BufferedImage.class));
        //then
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

}