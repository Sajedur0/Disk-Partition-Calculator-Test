import 'dart:math' as math;
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'circular_reveal_clipper.dart';

class ThemeRevealController extends StatefulWidget {
  final Widget Function(BuildContext context) builder;
  const ThemeRevealController({super.key, required this.builder});

  @override
  State<ThemeRevealController> createState() => ThemeRevealControllerState();

  static ThemeRevealControllerState of(BuildContext context) {
    final state = context.findAncestorStateOfType<ThemeRevealControllerState>();
    assert(
      state != null,
      'ThemeRevealController not found. Wrap your app root with it.',
    );
    return state!;
  }
}

class ThemeRevealControllerState extends State<ThemeRevealController>
    with SingleTickerProviderStateMixin {
  final GlobalKey _repaintKey = GlobalKey();
  late final AnimationController _controller;
  Animation<double>? _radiusAnim;

  Offset _center = Offset.zero;
  double _maxRadius = 0;
  ui.Image? _oldSnapshot;
  bool _isRevealing = false;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );
  }

  Future<void> revealFrom(
    Offset tapPosition,
    VoidCallback switchThemeState,
  ) async {
    if (_isRevealing) return;

    final boundary =
        _repaintKey.currentContext?.findRenderObject()
            as RenderRepaintBoundary?;
    if (boundary == null) {
      switchThemeState();
      return;
    }

    ui.Image? image;
    try {
      final ratio = math.min(1.0, MediaQuery.of(context).devicePixelRatio);
      image = await boundary.toImage(pixelRatio: ratio);

      if (!mounted) {
        image.dispose();
        return;
      }

      final size = MediaQuery.of(context).size;
      final corners = [
        Offset.zero,
        Offset(size.width, 0),
        Offset(0, size.height),
        Offset(size.width, size.height),
      ];
      final maxDist = corners
          .map((c) => (c - tapPosition).distance)
          .reduce(math.max);

      switchThemeState();

      setState(() {
        _oldSnapshot = image;
        _center = tapPosition;
        _maxRadius = maxDist;
        _isRevealing = true;
      });

      _radiusAnim = Tween<double>(
        begin: 0,
        end: _maxRadius,
      ).animate(
        CurvedAnimation(parent: _controller, curve: Curves.easeOutCubic),
      );

      _controller.reset();
      await _controller.forward();
    } catch (e) {
      debugPrint('ThemeRevealController error: $e');
      if (image != null && _oldSnapshot == null) {
        image.dispose();
      }
      switchThemeState();
    } finally {
      if (mounted) {
        setState(() {
          _isRevealing = false;
          _oldSnapshot?.dispose();
          _oldSnapshot = null;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final liveTree = RepaintBoundary(
      key: _repaintKey,
      child: widget.builder(context),
    );

    if (!_isRevealing || _oldSnapshot == null) return liveTree;

    return Stack(
      alignment: Alignment.topLeft,
      children: [
        Positioned.fill(
          child: RawImage(image: _oldSnapshot, fit: BoxFit.cover),
        ),
        AnimatedBuilder(
          animation: _radiusAnim!,
          builder: (context, child) {
            return ClipPath(
              clipper: CircularRevealClipper(
                center: _center,
                radius: _radiusAnim!.value,
              ),
              child: child,
            );
          },
          child: liveTree,
        ),
      ],
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    _oldSnapshot?.dispose();
    super.dispose();
  }
}
