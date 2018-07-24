const setup = function () {
    const canvas = $("#main-canvas");
    const context = canvas.get(0).getContext("2d");
    canvas.get(0).width = $(window).width();
    canvas.get(0).height = $(window).height();

    let points = [];

    canvas.bind("touchstart", function () {
        const x = event.pageX;
        const y = event.pageY;
        points = [{point: {x: x, y: y, z: 0.0, r: 0.0}, param: Date.now() * 10e-4}];
        $("#svg-area").empty();
    });

    canvas.bind("touchmove", function (event) {
        event.preventDefault();
        const x = event.changedTouches[0].pageX;
        const y = event.changedTouches[0].pageY;
        if (0 < points.length) {
            const from = points[points.length - 1];
            context.beginPath();
            context.moveTo(from.point.x, from.point.y);
            context.lineTo(x, y);
            context.closePath();
            context.stroke();
            points.push({point: {x: x, y: y, z: 0.0, r: 0.0}, param: Date.now() * 10e-4});
        }
    });

    canvas.bind("touchend", function () {
        console.log("end");
        const json = `{"canvas":{"x":${$(window).width()},"y":${$(window).height()}},"points":${JSON.stringify(points)}}`;
        send(json);
        points = [];
    });

    canvas.mousedown(function (event) {
        const x = event.pageX;
        const y = event.pageY;
        points = [{point: {x: x, y: y, z: 0.0, r: 0.0}, param: Date.now() * 10e-4}];
        $("#svg-area").empty();
    });

    canvas.mousemove(function (event) {
        const x = event.pageX;
        const y = event.pageY;
        if (0 < points.length) {
            const from = points[points.length - 1];
            context.beginPath();
            context.moveTo(from.point.x, from.point.y);
            context.lineTo(x, y);
            context.closePath();
            context.stroke();
            points.push({point: {x: x, y: y, z: 0.0, r: 0.0}, param: Date.now() * 10e-4})
        }
    });

    canvas.mouseup(function () {
        const json = `{"canvas":{"x":${$(window).width()},"y":${$(window).height()}},"points":${JSON.stringify(points)}}`;
        send(json);
        points = [];
    });
};

const send = function (json) {
    $.post("/fsci", json)
        .done(function (data) {
            const canvas = $("#main-canvas");
            const context = canvas.get(0).getContext("2d");
            const parser = new DOMParser();
            const dom = parser.parseFromString(data, "text/xml");
            $("#svg-area").append(dom.documentElement);
            context.clearRect(0, 0, canvas.get(0).width, canvas.get(0).height);
        })
};