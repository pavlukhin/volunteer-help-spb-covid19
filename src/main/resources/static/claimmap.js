claimsCtx = {};

ymaps.ready(init);

function init() {
    claimsCtx.map = new ymaps.Map("map", {
        center: [59.919486, 30.442504],
        zoom: 11,
        controls: ["zoomControl"]
    });

    var xhr = new XMLHttpRequest();
    // t0d0 check lambdas safety
    xhr.onload = () => {
        // t0d0 reflect explicitly if there is no free claims
        claimsCtx.claims = JSON.parse(xhr.responseText);
        showMarks();
        document.getElementById("openClaimsCb").onclick = showMarks;
        document.getElementById("inProgressClaimsCb").onclick = showMarks;
    };
    xhr.open("GET", "/api/claims");
    xhr.send();
}

function showMarks() {
    claimsCtx.map.geoObjects.removeAll();
    claimsCtx.claims.forEach(claim => {
        if (claim.status === "OPEN" && showOpenClaims()) {
            putClaimMark(claim, claimsCtx.map);
        }
        if (claim.status === "IN_PROGRESS" && showInProgressClaims()) {
            putClaimMark(claim, claimsCtx.map);
        }
    });
}

function showOpenClaims() {
    return document.getElementById("openClaimsCb").checked;
}

function showInProgressClaims() {
    return document.getElementById("inProgressClaimsCb").checked;
}

function putClaimMark(claim, activeMap) {
    var claimInfo = getClaimInfo(claim);
    if (claimInfo) {
      // t0d0 use Placemark?
      // t0d0 handle claims with a same address
      var gobj = new ymaps.GeoObject(
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
      activeMap.geoObjects.add(gobj);
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