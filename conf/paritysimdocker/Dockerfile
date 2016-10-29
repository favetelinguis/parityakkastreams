FROM openjdk
MAINTAINER Henrik Larsson
COPY target/parity-sim-0.1.0-SNAPSHOT.jar /opt/parity/paritysimulator010.jar
COPY etc/devel.conf /opt/parity/devel.conf
CMD ["java", "-jar", "/opt/parity/paritysimulator010.jar", "/opt/parity/devel.conf"]
# NOT SURE WHICH PORT I NEED TO OPEN HERE?
# Market data multicast
EXPOSE 5000
# Market data request
EXPOSE 5001
# Market report multicast
EXPOSE 6000
# Market report request
EXPOSE 6001
# Order entry
EXPOSE 4000
