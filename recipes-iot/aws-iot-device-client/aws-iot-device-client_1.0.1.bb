# -*- mode: Conf; -*-
SUMMARY = "AWS IoT Device Client"
DESCRIPTION = "The AWS IoT Device Client is free, open-source, modular software written in C++ that you can compile and install on your Embedded Linux based IoT devices to access AWS IoT Core, AWS IoT Device Management, and AWS IoT Device Defender features by default."
HOMEPAGE = "https://github.com/awslabs/aws-iot-device-client"
LICENSE = "Apache-2.0"
PROVIDES += "aws/aws-iot-device-client"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3eb31626add6ada64ff9ac772bd3c653"

BRANCH ?= "main"

# NOTE: This is v1.0 PLUS a fix for stripping the output binary.  it is why there is no
#       tag reference.
SRC_URI = "git://github.com/awslabs/aws-iot-device-client.git;protocol=https;branch=${BRANCH};bareclone=0"
SRCREV = "16b73b81da29149581a433cf7b6e69fcdd11176a"

S= "${WORKDIR}/git"
DEPENDS = "openssl aws-iot-device-sdk-cpp-v2 googletest"
RDEPENDS:${PN} = "openssl aws-iot-device-sdk-cpp-v2"
PREFERRED_VERSION_aws-iot-device-sdk-cpp-v2 = "1.10.5"

inherit cmake

do_configure:append() {
}

do_install() {
  install -d ${D}${base_sbindir}
  install -d ${D}${sysconfdir}
  install -d ${D}${systemd_unitdir}/system

  install -m 0755 ${WORKDIR}/build/aws-iot-device-client \
                  ${D}${base_sbindir}/aws-iot-device-client
  install -m 0644 ${S}/setup/aws-iot-device-client.service \
                  ${D}${systemd_system_unitdir}/aws-iot-device-client.service
  install -m 0644 ${S}/config-template.json \
                  ${D}${sysconfdir}/aws-iot-device-client.json
  
  sed -i -e "s,/sbin/aws-iot-device-client,/sbin/aws-iot-device-client --config /etc/aws-iot-device-client.json,g" \
    ${D}${systemd_system_unitdir}/aws-iot-device-client.service

}

AWSIOTDC_EXCL_JOBS ?= "OFF"
AWSIOTDC_EXCL_DD ?= "OFF"
AWSIOTDC_EXCL_ST ?= "OFF"
AWSIOTDC_EXCL_FP ?= "OFF"

OECMAKE_BUILDPATH += "${WORKDIR}/build"
OECMAKE_SOURCEPATH += "${S}"
EXTRA_OECMAKE += "-DBUILD_SDK=OFF"
EXTRA_OECMAKE += "-DBUILD_TEST_DEPS=OFF"
EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"
EXTRA_OECMAKE += "-DCMAKE_BUILD_TYPE=Release"
EXTRA_OECMAKE += "-DCMAKE_VERBOSE_MAKEFILE=ON"
EXTRA_OECMAKE += "-DCMAKE_CXX_FLAGS_RELEASE=-s"
EXTRA_OECMAKE += "-DEXCLUDE_JOBS=${AWSIOTDC_EXCL_JOBS}"
EXTRA_OECMAKE += "-DEXCLUDE_DD=${AWSIOTDC_EXCL_DD}"
EXTRA_OECMAKE += "-DEXCLUDE_ST=${AWSIOTDC_EXCL_ST}"
EXTRA_OECMAKE += "-DEXCLUDE_FP=${AWSIOTDC_EXCL_FP}"

FILES:${PN} += "${base_sbindir}/sbin/aws-iot-device-client"
FILES:${PN} += "${systemd_system_unitdir}/aws-iot-device-client.service"
FILES:${PN} += "${sysconfdir}/aws-iot-device-client.json"

INSANE_SKIP:${PN}:append = "already-stripped"

inherit systemd
SYSTEMD_AUTO_ENABLE = "enable"
SYSTEMD_SERVICE:${PN} = "aws-iot-device-client.service"


