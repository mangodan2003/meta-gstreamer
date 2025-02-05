SUMMARY = "VA-API support to GStreamer"
HOMEPAGE = "https://gstreamer.freedesktop.org/"
DESCRIPTION = "gstreamer-vaapi consists of a collection of VA-API \
based plugins for GStreamer and helper libraries: `vaapidecode', \
`vaapiconvert', and `vaapisink'."

PNREAL = "gstreamer-vaapi"

LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "https://gstreamer.freedesktop.org/src/${PNREAL}/${PNREAL}-${PV}.tar.xz"

SRC_URI[sha256sum] = "c8f015eee7a4d989383e0689a099bfabec95e55159ee875cc4eb9ff8d816dccb"

S = "${WORKDIR}/${PNREAL}-${PV}"
DEPENDS = "libva gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-bad"

inherit meson pkgconfig features_check upstream-version-is-even

REQUIRED_DISTRO_FEATURES ?= "opengl"

EXTRA_OEMESON += " \
    -Ddoc=disabled \
    -Dexamples=disabled \
    -Dtests=enabled \
"

PACKAGES =+ "${PN}-tests"

# OpenGL packageconfig factored out to make it easy for distros
# and BSP layers to pick either glx, egl, or no GL. By default,
# try detecting X11 first, and if found (with OpenGL), use GLX,
# otherwise try to check if EGL can be used.
PACKAGECONFIG_GL ?= "${@bb.utils.contains('DISTRO_FEATURES', 'x11 opengl', 'glx', \
                        bb.utils.contains('DISTRO_FEATURES',     'opengl', 'egl', \
                                                                       '', d), d)}"

PACKAGECONFIG ??= "drm encoders \
                   ${PACKAGECONFIG_GL} \
                   ${@bb.utils.filter('DISTRO_FEATURES', 'wayland x11', d)}"

PACKAGECONFIG[drm] = "-Ddrm=enabled,-Ddrm=disabled,udev libdrm"
PACKAGECONFIG[egl] = "-Degl=enabled,-Degl=disabled,virtual/egl"
PACKAGECONFIG[encoders] = "-Dencoders=enabled,-Dencoders=disabled"
PACKAGECONFIG[glx] = "-Dglx=enabled,-Dglx=disabled,virtual/libgl"
PACKAGECONFIG[wayland] = "-Dwayland=enabled,-Dwayland=disabled,wayland-native wayland wayland-protocols"
PACKAGECONFIG[x11] = "-Dx11=enabled,-Dx11=disabled,virtual/libx11 libxrandr libxrender"

FILES:${PN} += "${libdir}/gstreamer-*/*.so"
FILES:${PN}-dbg += "${libdir}/gstreamer-*/.debug"
FILES:${PN}-dev += "${libdir}/gstreamer-*/*.a"
FILES:${PN}-tests = "${bindir}/*"

# Commented out as there are no pkgconfig files in the image dir, and we don't need them

# # correct .pc install location - fixed in upstream trunk
# do_install:append() {
#     mkdir -p ${D}/${libdir}/pkgconfig
#     mv ${D}/${libdir}/gstreamer-1.0/pkgconfig/*.pc ${D}/${libdir}/pkgconfig
#     rmdir ${D}/${libdir}/gstreamer-1.0/pkgconfig/
# }
