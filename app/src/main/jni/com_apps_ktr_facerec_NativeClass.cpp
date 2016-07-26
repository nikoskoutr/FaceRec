#include <com_apps_ktr_facerec_NativeClass.h>
#include <opencv2/core/core.hpp>
#include <opencv2/face.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/imgproc.hpp"
#include <jni.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <sys/time.h>
#include <ctime>
#include <android/log.h>

using namespace cv;
using namespace std;
using namespace cv::face;

typedef long long int64;
typedef unsigned long long uint64;
// Get the tim in milliseconds, used to count time it takes for learning and predicting
static uint64 GetTimeMs64() {
struct timeval tv;

gettimeofday(&tv, NULL);

uint64 ret = tv.tv_usec;
/* Convert from micro seconds (10^-6) to milliseconds (10^-3) */
ret /= 1000;

/* Adds the seconds (10^0) after converting them to milliseconds (10^-3) */
ret += (tv.tv_sec * 1000);

return ret;
}

static void writeTimesFile(string filename, string algorithm, long elapsedTimeTrain, long elapsedTimePredict) {
    fstream appendFileToWorkWith;
    appendFileToWorkWith.open(filename.c_str(),  fstream::in | fstream::out | fstream::trunc);
    appendFileToWorkWith << "algorithm=" << algorithm << "\n";
    appendFileToWorkWith << "train=" << elapsedTimeTrain << "\n";
    appendFileToWorkWith << "predict=" << elapsedTimePredict;
    appendFileToWorkWith.close();
}

// Normalize image to 0-255 range
static Mat norm_0_255(InputArray _src) {
    Mat src = _src.getMat();
    // Create and return normalized image:
    Mat dst;
    switch(src.channels()) {
    case 1:
        cv::normalize(_src, dst, 0, 255, NORM_MINMAX, CV_8UC1);
        break;
    case 3:
        cv::normalize(_src, dst, 0, 255, NORM_MINMAX, CV_8UC3);
        break;
    default:
        src.copyTo(dst);
        break;
    }
    return dst;
}

// Read the csv file
static void read_csv(const string& filename, vector<Mat>& images, vector<int>& labels, char separator = ';') {
    std::ifstream file(filename.c_str(), ifstream::in);
    if (!file) {
        string error_message = "No valid input file was given, please check the given filename.";
        CV_Error(CV_StsBadArg, error_message);
    }
    string line, path, classlabel;
    while (getline(file, line)) {
        stringstream liness(line);
        getline(liness, path, separator);
        getline(liness, classlabel);
        if(!path.empty() && !classlabel.empty()) {
            images.push_back(imread(path, 0));
            labels.push_back(atoi(classlabel.c_str()));
        }
    }
}


// Train the eigenface algorithm and save to file
static void trainEigenFaces(vector<Mat>& i, vector<int>& l, std::string& applicationPath) {
    String trainedEigenfacesFile = applicationPath + "/eigenfaces.yml";
    Ptr<FaceRecognizer> model;
    model = createEigenFaceRecognizer();
    model->train(i,l);
    model->save(trainedEigenfacesFile);
    return;
}

// Train the fisherface algorithm and save to file
static void trainFisherFaces(vector<Mat>& i, vector<int>& l, std::string& applicationPath) {
    String trainedFisherfacesFile = applicationPath + "/fisherfaces.yml";
    Ptr<FaceRecognizer> model;
    model = createFisherFaceRecognizer();
    model->train(i,l);
    model->save(trainedFisherfacesFile);
    return;
}

// Train the LBPH algorithm and save to file
static void trainLBPH(vector<Mat>& i, vector<int>& l, std::string& applicationPath) {
    String trainedLBPHFile = applicationPath + "/LBPH.yml";
    Ptr<FaceRecognizer> model;
    model = createLBPHFaceRecognizer();
    model->train(i,l);
    model->save(trainedLBPHFile);
    return;
}

// Predict using the LBPH algorithm
static jstring predictLBPH(string& path, jboolean& useData, vector<Mat>& images, vector<int>& labels, Mat& test, JNIEnv * env, string applicationPath) {
    long elapsedTimeTrain;
    long elapsedTimePredict;
    string algorithm = "LBPH";
    Ptr<LBPHFaceRecognizer> model = createLBPHFaceRecognizer();
    if (useData) {
        model->load(path);
    } else {
        uint64 startTime = GetTimeMs64();
        model->train(images, labels);
        elapsedTimePredict = GetTimeMs64() - startTime;
    }
    uint64 startTime = GetTimeMs64();
    int predictedLabel = model->predict(test);
    elapsedTimePredict = GetTimeMs64() - startTime;

    string filename = applicationPath + "/times.txt";
    writeTimesFile(filename, algorithm, elapsedTimeTrain, elapsedTimePredict);

    string result;
    ostringstream convert;
    convert << predictedLabel;
    result = convert.str();
    return env->NewStringUTF(result.c_str());
}

static void GetJStringContent(JNIEnv *AEnv, jstring AStr, std::string &ARes) {
  if (!AStr) {
    ARes.clear();
    return;
  }

  const char *s = AEnv->GetStringUTFChars(AStr,NULL);
  ARes=s;
  AEnv->ReleaseStringUTFChars(AStr,s);
}

JNIEXPORT jstring JNICALL Java_com_apps_ktr_facerec_NativeClass_nativeFunction (JNIEnv * env, jobject obj, jint function, jboolean useTrainedData, jstring externalstorage) {
    // Constants to define native functionality
    std::string absolutePath;
    GetJStringContent(env, externalstorage, absolutePath);
    std::string applicationPath = absolutePath + "/FaceRec";
    const int eigenfaces = 1;
    const int fisherfaces = 2;
    const int LBPH = 3;
    const int trainEigenface = 4;
    const int trainFisherface = 5;
    const int trainLbph = 6;
    string algorithm;
    string testImage = applicationPath + "/test.png";
    string csv = applicationPath + "/images.csv";
    string trainedEigenfacesFile = applicationPath + "/eigenfaces.yml";
    string trainedFisherfacesFile = applicationPath + "/fisherfaces.yml";
    string trainedLBPHFile = applicationPath + "/LBPH.yml";
    long startTime;
    long elapsedTimeTrain = 0;
    long elapsedTimePredict = 0;

    // Vectors that will store the images and the labels that will be used
    vector<Mat> images;
    vector<int> labels;

    // Try reading the csv file
    try {
        read_csv(csv, images, labels);
    } catch (cv::Exception& e) {
        cerr << "Error opening file \"" << csv << "\". Reason: " << e.msg << endl;
        // nothing more we can do
        exit(1);
    }

    // Get the image that will be tested and get its label predicted
    Mat test = imread(testImage, 0);

    // Basic face recognizer that will be either eigenfaces recognizer or fisherfaces
    Ptr<BasicFaceRecognizer> model;
    switch (function) {
        case eigenfaces:
            model = createEigenFaceRecognizer();
            algorithm = "eigenfaces";
            break;
        case fisherfaces:
            model = createFisherFaceRecognizer();
            algorithm = "fisherfaces";
            break;
        case LBPH:
            // LBPH is not a basic face recognizer so special treatment is needed
            return predictLBPH(trainedLBPHFile, useTrainedData, images, labels, test, env, applicationPath);
            break;
        case trainEigenface:
            trainEigenFaces(images, labels, applicationPath);
            return env->NewStringUTF("-1");
        case trainFisherface:
            trainFisherFaces(images, labels, applicationPath);
            return env->NewStringUTF("-1");
        case trainLbph:
            trainLBPH(images, labels, applicationPath);
            return env->NewStringUTF("-1");
    }
    if (useTrainedData) {
        switch (function) {
            case eigenfaces:
                model->load(trainedEigenfacesFile);
                break;
            case fisherfaces:
                model->load(trainedFisherfacesFile);
                break;
        }
    } else {
        startTime = GetTimeMs64();
        model->train(images, labels);
        elapsedTimeTrain = GetTimeMs64() - startTime;
    }

    // Predict the label of the image
    startTime = GetTimeMs64();
    int predictedLabel = model->predict(test);
    elapsedTimePredict = GetTimeMs64() - startTime;
    Mat eigenvalues = model->getEigenValues();
    Mat eigenvectors = model->getEigenVectors();
    Mat mean = model->getMean();
    int num_components_used = model->getNumComponents();

    // Create file with time consumed for training and predicting
    string filename = applicationPath + "/times.txt";
    writeTimesFile(filename, algorithm, elapsedTimeTrain, elapsedTimePredict);



    // Create the eigenfaces and save them
    for (int i = 0; i < min(10, eigenvectors.cols); i++) {
        Mat ev = eigenvectors.col(i).clone();
        Mat grayscale = norm_0_255(ev.reshape(1, test.rows));
        Mat cgrayscale;
        applyColorMap(grayscale, cgrayscale, COLORMAP_JET);
        imwrite(format("%s/eigenface_%d.png", applicationPath.c_str(), i), norm_0_255(cgrayscale));
    }


    Mat evs = Mat(eigenvectors, Range::all(), Range(0, num_components_used));
    Mat projection = LDA::subspaceProject(evs, mean, test.reshape(1,1));
    Mat reconstruction = LDA::subspaceReconstruct(evs, mean, projection);
    reconstruction = norm_0_255(reconstruction.reshape(1, test.rows));
    imwrite(format("%s/reconstruction_actually_used_components_%d.png", applicationPath.c_str(), num_components_used), reconstruction);

    // Reconstruct the test image with the number of components that the algorithm used
    evs = Mat(eigenvectors, Range::all(), Range::all());
    projection = LDA::subspaceProject(evs, mean, test.reshape(1,1));
    reconstruction = LDA::subspaceReconstruct(evs, mean, projection);
    reconstruction = norm_0_255(reconstruction.reshape(1, test.rows));
    imwrite(format("%s/reconstruction_max_used_components_%d.png", applicationPath.c_str(), eigenvectors.cols), reconstruction);

    // Convert to string and return
    string result;
    ostringstream convert;
    convert << predictedLabel;
    result = convert.str();
    return env->NewStringUTF(result.c_str());
}