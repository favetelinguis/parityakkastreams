FROM openjdk
MAINTAINER Henrik Larsson
COPY target/parity-system-0.5.1-SNAPSHOT.jar /opt/parity/parity051.jar
COPY etc/devel.conf /opt/parity/devel.conf
CMD ["java", "-jar", "/opt/parity/parity051.jar", "/opt/parity/devel.conf"]
EXPOSE 5000 #Market data multicast
EXPOSE 5001 #Market data request
EXPOSE 6000 #Market report multicast
EXPOSE 6001 #Market report request
EXPOSE 4000 #Order entry
