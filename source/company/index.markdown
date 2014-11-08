---
layout: page
title: "company"

comments: false
sharing: false
footer: true
---


{% capture company %}{% include pages/company.md %}{% endcapture %}
{{ company | markdownify }}


<div class="divider divider-large"></div>

## Partners
{% capture typesafe %}{% include pages/typesafe.md %}{% endcapture %}
{{ typesafe | markdownify }}  

<div class="divider divider-large"></div>


## Who are we?
{% include pages/team.html %}