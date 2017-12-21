# AnomalyDetection

Includes

	•	Android code written in Java
	•	Machine learning code written in Python

About project 

An android application that uses GPS to track personal trajectories and sends an alert whenever an anomaly is detected in the trajectory. Anomaly detection is one important aspect that has been researched within diverse research areas and application domains. We explore the same in our project and also analyze the precision and applications. We also have looked into computational complexity while selecting our algorithm keeping in mind the time and space constraints of anomaly detection on live GPS data.


Getting Started

.apk file can be directly installed in any GPPS enabled android smartphone.

Deployment

For live anomaly detection, the GPS data must be sent to a server where the anomaly detection algorithm resides. It fetches live data continuously and computed anomaly score based on your current location. If the anomaly score exceeds a threshold, it generates an alert and sent it to the application running in the phone.

Built With

Android Studio - For the android application
Pycharm - For the machine learning model
AWS server - For running the model and generate an alert
