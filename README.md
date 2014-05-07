AndroidLinearAcceleration
=========================

![](http://www.kircherelectronics.com/bundles/keweb/css/images/android_linear_acceleration_phone_graphic.png?raw=true)

Android Linear Acceleration provides a working open source code example and Android application that demonstrates implementing the sensor TYPE_LINEAR_ACCELERATION.

Android Linear Acceleration is intended to provide developers with code examples and an application to quickly test devices for an implementation of Sensor.TYPE_LINEAR_ACCELERATION. Some Android devices provide an implementation of linear acceleration with Sensor.TYPE_LINEAR_ACCELERATION and others do not. The implementation and performance of Sensor.TYPE_LINEAR_ACCELERATION varies from device to device. Some devices rely on only the acceleration sensor via low-pass filters or other methods. Devices equiped with a gyroscopes will fuse the acceleration sensor to provide an estimation of linear acceleration. 

Almost all implementations of Sensor.TYPE_LINEAR_ACCELERATION are not ideal. The implementations succesfully isolate gravity from the acceleration under static conditions. However, while the device is actually under linear acceleration, the estimation tends to become skewed. This means that attempting to measure the acceleration of a vehicle, for instance, using Sensor.TYPE_LINEAR_ACCELERATION will result in measurements that are inaccurate.

On devices that implement linear acceleration with a low-pass filter, the continuous acceleration causes the low-pass filter to confuse the gravity estimation with the linear acceleration. This causes the estimation of the orientation of the device to become skewed, which in turn causes the linear acceleration estimation to be inaccurate.

On devices that implement linear acceleration with a gyroscope and acceleration sensor fusion, a complimentary filter is often used. The complimentary filter uses the acceleration sensor to compenstate for the drift of the gyroscope. Under extend periods of linear acceleration, the acceleration sensor is not reliable for drift compensations and begins to compensate the gyroscope for drift erroneously. The errorneous drift compensations skew the orientation estimations, which in turn causes the linear acceleration estimations to be inaccurate. 

Both the low-pass filter and gyroscope fusion's shortcomings apply not just to linear acceleration, but also to estimating rotational displacement, velocity and acceleration.

Android Linear Acceleration allows developers and other interested parties to determine if a specific device implements Sensor.TYPE_LINEAR_ACCELERATION, to explore the performance of the sensor under different conditions and to compare the sensor to other implementations.

Features:

* Plot all axes of the linear acceleration sensor in real-time
* Log all axes of the linear acceleration sensor to a .CSV file
* Examine the peformance of the linear acceleration sensor under different conditions
* Compare the performance of the linear acceleration sensor to other implementations

Useful Links:

* [Android Linear Acceleration Homepage](http://www.kircherelectronics.com/androidlinearacceleration/androidlinearacceleration)
* [Download Android Linear Acceleration from Google Play](https://play.google.com/store/apps/details?id=com.kircherelectronics.androidlinearacceleration)

Written by [Kircher Electronics](https://www.kircherelectronics.com)
