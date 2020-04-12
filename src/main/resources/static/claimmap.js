claimsCtx = {};

ymaps.ready(init);

function init() {
    claimsCtx.map = new ymaps.Map("map", {
        center: [59.919486, 30.442504],
        zoom: 11,
        controls: ["zoomControl"]
    });

    var goButton = new ymaps.control.Button({
        data: {
            content: "Поехали!"
        },
        options: {
            selectOnClick: false
        }
    });
    goButton.events.add("click", () => {
        var map0 = claimsCtx.map;
        var balloon = map0.balloon;
        var bData = balloon.getData();
        var gobj;
        if (bData) {
            if (bData.geoObject) {
                gobj = bData.geoObject;
            }
            else if (bData.cluster) {
                gobj = bData.cluster.state.get("activeObject");
            }
        }
        if (balloon.isOpen() && gobj) {
            var routeLink = buildRouteLink(gobj, map0.getCenter(), map0.getZoom());
            window.open(routeLink);
        }
        else {
            window.alert("Выберите заявку!");
        }
    });
    claimsCtx.map.controls.add(goButton);

    var xhr = new XMLHttpRequest();
    // t0d0 check lambdas safety
    xhr.onload = () => {
        // t0d0 reflect explicitly if there is no free claims
        var allClaims = JSON.parse(xhr.responseText);

        var openClaims = new ymaps.Clusterer({
            clusterDisableClickZoom: true,
            preset: "islands#greenClusterIcons"
        });
        openClaims.add(allClaims.filter(c => c.status === "OPEN").map(c => getClaimMark(c)));
        claimsCtx.openClaims = openClaims;

        var inProgressClaims = new ymaps.Clusterer({
            clusterDisableClickZoom: true,
            preset: "islands#orangeClusterIcons"
        });
        inProgressClaims.add(allClaims.filter(c => c.status === "IN_PROGRESS").map(c => getClaimMark(c)));
        claimsCtx.inProgressClaims = inProgressClaims;

        showMarks();
        document.getElementById("openClaimsCb").onclick = showMarks;
        document.getElementById("inProgressClaimsCb").onclick = showMarks;
    };
    xhr.open("GET", "/api/claims");
    xhr.send();
}

function showMarks() {
    claimsCtx.map.geoObjects.removeAll();
    if (showOpenClaims()) {
        claimsCtx.map.geoObjects.add(claimsCtx.openClaims);
    }
    if (showInProgressClaims()) {
        claimsCtx.map.geoObjects.add(claimsCtx.inProgressClaims);
    }
}

function showOpenClaims() {
    return document.getElementById("openClaimsCb").checked;
}

function showInProgressClaims() {
    return document.getElementById("inProgressClaimsCb").checked;
}

function getClaimMark(claim, activeMap) {
    // t0d0 handle invalid claim info properly
    var claimInfo = getClaimInfo(claim);
    if (claimInfo) {
      // t0d0 use Placemark?
      // t0d0 handle claims with a same address
      return new ymaps.GeoObject(
        feature = {
          geometry: {
            type: "Point",
            coordinates: claim.coord
//            coordinates: [59.919486, 30.442504]
          },
          properties: {
            iconCaption: claim.id,
            balloonContentHeader: claim.id,
            balloonContentBody: claimInfo.balloonText
          }
        },
        options = {
          iconColor: claimInfo.statusColor
        }
      );
    }
}

function getClaimInfo(claim) {
    var claimInfo = {};
    if (claim.status === "OPEN") {
        claimInfo.statusColor = "green"
    }
    else if (claim.status === "IN_PROGRESS") {
        claimInfo.statusColor = "orange";
    }
    else {
        console.log("Unknown claim status " + JSON.stringify(claim));
        return null;
    }
    // t0d0 escape internal html?
    claimInfo.balloonText = claim.address + "<br>" + claim.details;
    return claimInfo;
}

function buildRouteLink(toObj, center, zoom) {
    var toCrd = toObj.geometry.getCoordinates();
    return "https://yandex.ru/maps/?z=" + zoom + "&ll=" + center[1] + "," + center[0] + "&l=map&rtext=~" + toCrd[0] + "," + toCrd[1] + "&origin=jsapi_2_1_76&from=api-maps";
}