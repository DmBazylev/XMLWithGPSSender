#include "xmlwithgpssender.h"

XMLWithGPSSender::XMLWithGPSSender(QWidget *parent)
    : QWidget(parent)
{
}

XMLWithGPSSender::~XMLWithGPSSender()
{
}

void XMLWithGPSSender::getRECEIVE_SMS()
{
    QtAndroid::requestPermissionsSync(QStringList() << "android.permission.RECEIVE_SMS");
}

void XMLWithGPSSender::getSEND_SMS()
{
    QtAndroid::requestPermissionsSync(QStringList() << "android.permission.SEND_SMS");
}

void XMLWithGPSSender::getPermissionFINE_LOCATION()
{
    QtAndroid::requestPermissionsSync(QStringList() << "android.permission.ACCESS_FINE_LOCATION");
}

void XMLWithGPSSender::getPermissionREAD_EXTERNAL_STORAGE()
{
    QtAndroid::requestPermissionsSync(QStringList() << "android.permission.READ_EXTERNAL_STORAGE");
}

void XMLWithGPSSender::getPermissionWRITE_EXTERNAL_STORAGE()
{
    QtAndroid::requestPermissionsSync(QStringList() << "android.permission.WRITE_EXTERNAL_STORAGE");
}
