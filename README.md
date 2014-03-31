AndroidLinearAcceleration
=========================

Android Linear Acceleration provides a working code example and Android application that demonstrates implementing the sensor TYPE_LINEAR_ACCELERATION.

Android Linear Acceleration is intended to provide developers with code examples and an application to quickly test devices for an implementation of Sensor.TYPE_LINEAR_ACCELERATION. Some Android devices provide an implementation of linear acceleration with Sensor.TYPE_LINEAR_ACCELERATION, others do not. The implementation and performance of Sensor.TYPE_LINEAR_ACCELERATION varies from device to device. Some devices rely on low-pass filters, some fuse the magnetic and acceleration sensors, some fuse the gyroscope and acceleration sensors and others do not implement linear acceleration at all. 

Almost all implementations of Sensor.TYPE_LINEAR_ACCELERATION are poor. The implementations succesfully isolate gravity from the acceleration under static conditions. However, while the device is actually under linear acceleration, the gravity compensation tends to be overestimated skewing the linear acceleration measurement. Android Linear Acceleration is ideal for discovering the limitations and performance of Sensor.TYPE_LINEAR_ACCELERATION.

Features:
• Log all of your data in real-time
• Analog gauges to display the outputs
• Real time sensor plots to visualize performance
