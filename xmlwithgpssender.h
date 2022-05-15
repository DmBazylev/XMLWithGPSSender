#ifndef XMLWITHGPSSENDER_H
#define XMLWITHGPSSENDER_H

#include <QWidget>

#include <QtAndroid>

class XMLWithGPSSender : public QWidget
{
    Q_OBJECT

public:
    XMLWithGPSSender(QWidget *parent = nullptr);
    ~XMLWithGPSSender();

    void getRECEIVE_SMS();
    void getSEND_SMS();
    void getPermissionFINE_LOCATION();
    void getPermissionREAD_EXTERNAL_STORAGE();
    void getPermissionWRITE_EXTERNAL_STORAGE();
};
#endif // XMLWITHGPSSENDER_H
