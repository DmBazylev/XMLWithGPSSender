#include "xmlwithgpssender.h"

#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    XMLWithGPSSender w;

    //получение разрешений в runtime режиме при запуске программы
    w.getRECEIVE_SMS();
    w.getSEND_SMS();
    w.getPermissionFINE_LOCATION();
    w.getPermissionREAD_EXTERNAL_STORAGE();
    w.getPermissionWRITE_EXTERNAL_STORAGE();

    return a.exec();
}
