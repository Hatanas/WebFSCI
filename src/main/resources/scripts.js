const setup = function () {
    const canvas = $("#maincanvas");
    const context = canvas.get(0).getContext("2d");
    canvas.get(0).width = $(window).width();
    canvas.get(0).height = $(window).height();

    let points = [];

    canvas.bind("touchstart", function (event) {
        points = [new InputPoint(event.changedTouches[0].pageX, event.changedTouches[0].pageY)];
        $("#svgarea").empty();
    });

    canvas.bind("touchmove", function (event) {
        event.preventDefault();
        if (0 < points.length) {
            const from = points[points.length - 1];
            const to = new InputPoint(event.changedTouches[0].pageX, event.changedTouches[0].pageY);
            drawInput(context, from, to);
            points.push(to);
        }
    });

    canvas.bind("touchend", function () {
        const json = `{"canvas":{"x":${$(window).width()},"y":${$(window).height()}},"points":${JSON.stringify(points)}}`;
        send(json);
        points = [];
    });

    canvas.mousedown(function (event) {
        points = [new InputPoint(event.pageX, event.pageY)];
        $("#svgarea").empty();
    });

    canvas.mousemove(function (event) {
        if (0 < points.length) {
            const from = points[points.length - 1];
            const to = new InputPoint(event.pageX, event.pageY);
            drawInput(context, from, to);
            points.push(to);
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
            const canvas = $("#maincanvas");
            const context = canvas.get(0).getContext("2d");
            const parser = new DOMParser();
            const dom = parser.parseFromString(data, "text/xml");
            $("#svgarea").append(dom.documentElement);
            context.clearRect(0, 0, canvas.get(0).width, canvas.get(0).height);
        })
};

const drawInput = function(context, from, to) {
    context.beginPath();
    context.moveTo(from.x, from.y);
    context.lineTo(to.x, to.y);
    context.closePath();
    context.stroke();
};

class InputPoint {
    constructor(x, y) {
        this.x = x;
        this.y = y;
        this.param = Date.now() * 10e-4;
    }
    toJSON() {
        return {point: {x: this.x, y: this.y, z: 0.0, r: 0.0}, param: this.param};
    }
}