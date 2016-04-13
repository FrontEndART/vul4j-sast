/*
 * Copyright (c) 2014 T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var $ = require('bootstrap-detached').getBootstrap();

if (Prototype.BrowserFeatures.ElementExtensions) {
    // Fix incompatibilities between BootStrap and Prototype
    var disablePrototypeJS = function (method, pluginsToDisable) {
            var handler = function (event) {
                event.target[method] = undefined;
                setTimeout(function () {
                    delete event.target[method];
                }, 0);
            };
            pluginsToDisable.each(function (plugin) {
                $(window).on(method + '.bs.' + plugin, handler);
            });
        },
        pluginsToDisable = ['collapse', 'dropdown', 'modal', 'tooltip', 'popover', 'tab'];
    disablePrototypeJS('show', pluginsToDisable);
    disablePrototypeJS('hide', pluginsToDisable);
}

$(document).ready(function () {
    $('.carousel').each(function (carouselIndex, carousel) {
        var testCase = $(carousel).attr('id').substring(9);

        projectAction.getDashboardConfiguration(testCase, function (data) {
            var json = JSON.parse(data.responseObject());
            $.each(json, function (index) {
                if (json[index].show && json[index].dashboard === testCase) {
                    if (json[index].id === 'unittest_overview') {
                        $('.carousel-inner', carousel).append('<div class="item">' +
                            '<img class="img-thumbnail" height="300" width="410"' +
                            'src="performance-signature/testRunGraph?width=410&amp;height=300"></div>\n');
                    } else {
                        $('.carousel-inner', carousel).append('<div class="item">' +
                            '<img class="img-thumbnail" height="300" width="410"' +
                            'src="performance-signature/summarizerGraph?width=410&amp;height=300&amp;id=' + json[index].id + '"></div>\n');
                    }
                }
            });
            $('.carousel-inner div:first-child', carousel).addClass('active');
            $('.carousel').carousel(0);
        });
    });

    var hash = window.location.hash;
    if (hash) {
        $('ul.nav a[href="' + hash + '"]').tab('show');
    } else {
        $('#tabList').find('a:first').tab('show'); // Select first tab
    }

    $('.nav-tabs a').click(function () {
        $(this).tab('show');
        var scrollmem = $('body').scrollTop() || $('html').scrollTop();
        window.location.hash = this.hash;
        $('html,body').scrollTop(scrollmem);
    });
});
