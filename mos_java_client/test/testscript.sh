#!/bin/bash

set -e

CLIMOS=./bin/climos


general_test() {
    $CLIMOS GetBalance
    $CLIMOS DescribeInstanceTypes
    $CLIMOS DescribeTemplates
    $CLIMOS DescribeInstances
    $CLIMOS DescribeAvailabilityZones
}

get_status() {
    local IID=$1
    $CLIMOS DescribeInstanceStatus $IID | grep 'status' | awk '{print $4}'
}

wait_instance_status() {
    local IID=$1
    local TARGET=$2
    local TIMEOUT=$3
    local WAIT_SEC=0
    while [ "$(get_status $IID)" != "$TARGET" ] && [ "$WAIT_SEC" -lt "$TIMEOUT" ]; do
        WAIT_SEC=$((WAIT_SEC+1))
        sleep 1
    done
    if [ "$(get_status $IID)" != "$TARGET" ]; then
        echo "$IID status is not $TARGET"
        exit 1
    fi
}

get_snapshot_status() {
    local SNAPID=$1
    $CLIMOS DescribeSnapshots | grep $SNAPID | awk '{print $6}'
}

wait_snapshot_status() {
    local SNAPID=$1
    local TARGET=$2
    local TIMEOUT=$3
    local WAIT_SEC=0
    while [ "$(get_snapshot_status $SNAPID)" != "$TARGET" ] && [ "$WAIT_SEC" -lt "$TIMEOUT" ]; do
        WAIT_SEC=$((WAIT_SEC+1))
        sleep 1
    done
    if [ "$(get_snapshot_status $SNAPID)" != "$TARGET" ]; then
        echo "$SNAPID status is not $TARGET"
        exit 1
    fi
}

create_instance() {
    IMGID=$($CLIMOS DescribeTemplates | grep "active" | head -n 1 | awk '{print $2}')
    FLAVOR=$($CLIMOS DescribeInstanceTypes | grep '[0-9a-fA-F]\{8\}-[0-9a-fA-F]\{4\}-[0-9a-fA-F]\{4\}-[0-9a-fA-F]\{4\}-[0-9a-fA-F]\{12\}' | sort -n -k 14 | head -n 1 | awk '{print $2}')
    echo "Use image $IMGID instance type $FLAVOR"
    $CLIMOS CreateInstance --duration 1H --name $NAME --datadisk 1 --bandwidth 1 $IMGID $FLAVOR
}

test_instance() {
    local IID=$1
    $CLIMOS DescribeInstanceVolumes $IID
    $CLIMOS DescribeInstanceNetworkInterfaces $IID
    $CLIMOS GetInstanceContractInfo $IID
    $CLIMOS GetPasswordData $IID
    $CLIMOS GetInstanceMetadata $IID
    local STATUS=$(get_status $IID)
    if [ "$STATUS" == "ready" ]; then
        $CLIMOS StartInstance $IID
        wait_instance_status $IID "running" 1800
    fi
    echo "Stop..."
    $CLIMOS StopInstance $IID
    wait_instance_status $IID "ready" 1800
    $CLIMOS StartInstance $IID
    wait_instance_status $IID "running" 1800
    $CLIMOS PutInstanceMetadata $IID --data "metakey:metaval"
    $CLIMOS GetInstanceMetadata $IID
    $CLIMOS RebuildInstanceRootImage $IID
    wait_instance_status $IID "running" 1800
    $CLIMOS RebootInstance $IID
    wait_instance_status $IID "running" 1800
    $CLIMOS RenewInstance $IID --duration 7H
    local IMG_NAME='temp-image'
    $CLIMOS CreateTemplate $IID $IMG_NAME
    wait_instance_status $IID "running" 1800
    local IMGID=$($CLIMOS DescribeTemplates | grep $IMG_NAME | awk '{print $2}')
    $CLIMOS DeleteTemplate $IMGID
    local SNAPSHOT_NAME="mysnapshot01"
    $CLIMOS CreateSnapshot $IID --name $SNAPSHOT_NAME
    wait_snapshot_status $SNAPSHOT_NAME ready 1800
    $CLIMOS RestoreSnapshot $IID $SNAPSHOT_NAME
    wait_instance_status $IID "running" 1800
    local SNAP_IID="${IID}snap"
    $CLIMOS CreateInstanceFromSnapshot $SNAPSHOT_NAME --name $SNAP_IID
    wait_instance_status $SNAP_IID "running" 1800
    $CLIMOS TerminateInstance $SNAP_IID
    $CLIMOS DeleteSnapshot $SNAPSHOT_NAME
}

instance_test() {
    local NAME='tmptestsrv'

    local STATUS=$(get_status $NAME)

    if [ -z "$STATUS" ]; then
        echo "Create $NAME ..."
        create_instance
        wait_instance_status $NAME "running" 1800
    fi
    test_instance $NAME
    $CLIMOS TerminateInstance $NAME
}

general_test
instance_test

echo "Test done!!"
