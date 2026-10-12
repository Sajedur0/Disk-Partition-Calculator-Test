import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../constants/app_constants.dart';
import '../app_theme.dart';

class PrivacyPolicyPage extends StatelessWidget {
  const PrivacyPolicyPage({super.key});

  static const _playStoreLink =
      'https://play.google.com/store/apps/details?id=com.diskcalc';
  static const _devPlayStoreLink =
      'https://play.google.com/store/apps/dev?id=5574132061091657048';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Privacy Policy'), centerTitle: false),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(24, 16, 24, 32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeader(context),
            const SizedBox(height: 28),
            _buildEffectiveDate(context),
            const SizedBox(height: 24),
            _buildSection(
              context,
              Icons.info_outline_rounded,
              'Introduction',
              '$appName ("we", "our", or "us") is committed to protecting your privacy. '
                  'This Privacy Policy explains how we collect, use, and safeguard your information '
                  'when you use our mobile application. By using $appName, you agree to the '
                  'collection and use of information in accordance with this policy.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.folder_shared_outlined,
              'Information We Collect',
              'We believe in minimal data collection. $appName collects the following:',
              bullets: [
                'Conversion History — Your past calculations are stored locally on your device for your convenience.',
              ],
              footer:
                  'We do NOT collect, store, or transmit any personal identifiable information '
                      'such as your name, email address, phone number, location, or device identifiers.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.settings_suggest_outlined,
              'How We Use Your Information',
              'The limited data we store is used solely to:',
              bullets: [
                'Provide accurate disk partition size calculations',
                'Maintain your local calculation history',
                'Improve app functionality and user experience',
              ],
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.security_rounded,
              'Data Storage & Security',
              'Your calculation history is stored exclusively on your device using Android\'s '
                  'local SharedPreferences storage. This data is never uploaded to any server, '
                  'cloud service, or external database. There are no remote databases, cloud '
                  'syncs, or API calls that transmit your data. Your device is the only place '
                  'your calculation history exists.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.hub_outlined,
              'Third-Party Services',
              '$appName does not use any third-party services. Specifically, we do not '
                  'integrate or transmit data to:',
              bullets: [
                'Analytics services (no Google Analytics, Firebase Analytics, or similar)',
                'Advertising or ad networks (no AdMob, Facebook Ads, or similar)',
                'Crash reporting tools (no Crashlytics, Sentry, or similar)',
                'Tracking or fingerprinting SDKs',
                'Social media SDKs',
              ],
              footer:
                  'The only network communication is for Google Play In-App Updates, which '
                      'checks for app version availability and does not involve your personal data.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.verified_user_outlined,
              'Data Deletion & User Choices',
              'You have full control over your data at all times:',
              bullets: [
                'Access — Browse all saved calculations via the History panel at any time.',
                'Delete Individual — Remove specific calculation entries from your history.',
                'Delete All — Use the Clear All option in the History panel to erase all stored data at once.',
                'Complete Removal — Uninstall the app to permanently delete all locally stored data from your device.',
              ],
              footer:
                  'Since all data is stored locally on your device and never transmitted externally, '
                      'no server-side deletion request is required. Removing the app removes everything.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.child_care_outlined,
              "Children's Privacy",
              '$appName is not directed at children. We do not knowingly collect personal data '
                  'from anyone, including minors. The age of digital consent varies by jurisdiction '
                  '— for example, 13 years in the United States (COPPA), 16 years in the European '
                  'Economic Area under GDPR, and other ages in different countries. Since this app '
                  'collects no personal information whatsoever, it is safe for users of all ages.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.update_rounded,
              'Changes to This Policy',
              'We may update this Privacy Policy from time to time. Any changes will be reflected '
                  'in this page and the "Last Updated" date will be revised. We encourage you to '
                  'review this policy periodically.',
            ),
            const SizedBox(height: 20),
            _buildSection(
              context,
              Icons.gavel_outlined,
              'Compliance',
              'This Privacy Policy is designed to comply with applicable privacy laws and '
                  'regulations, including the General Data Protection Regulation (GDPR) and '
                  'the California Consumer Privacy Act (CCPA). As we do not collect personal data, '
                  'most regulatory obligations are inherently satisfied.',
            ),
            const SizedBox(height: 28),
            _buildConsentBanner(context),
            const SizedBox(height: 28),
            _buildContactUs(context),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: appCardBorder(colorScheme),
        boxShadow: AppShadows.card(theme.brightness),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: colorScheme.primary.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(AppRadius.sm),
            ),
            child: Icon(
              Icons.shield_outlined,
              color: colorScheme.primary,
              size: 26,
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Privacy Policy',
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  appName,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEffectiveDate(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppRadius.sm),
      ),
      child: Text(
        'Effective Date: August 20, 2026',
        style: theme.textTheme.labelSmall?.copyWith(
          color: colorScheme.onSurfaceVariant,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  Widget _buildSection(
    BuildContext context,
    IconData icon,
    String title,
    String content, {
    List<String>? bullets,
    String? footer,
  }) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: colorScheme.primary.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(AppRadius.sm),
              ),
              child: Icon(icon, color: colorScheme.primary, size: 20),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                title,
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Text(
          content,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: colorScheme.onSurfaceVariant,
            height: 1.5,
          ),
        ),
        if (bullets != null) ...[
          const SizedBox(height: 10),
          ...bullets.map(
            (b) => Padding(
              padding: const EdgeInsets.only(bottom: 6),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.only(top: 7),
                    child: Icon(
                      Icons.circle,
                      size: 6,
                      color: colorScheme.primary,
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      b,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                        height: 1.5,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
        if (footer != null) ...[
          const SizedBox(height: 10),
          Text(
            footer,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: colorScheme.onSurfaceVariant,
              height: 1.5,
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildConsentBanner(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.primaryContainer.withValues(alpha: 0.4),
        borderRadius: BorderRadius.circular(AppRadius.md),
      ),
      child: Text(
        'By using $appName, you consent to this Privacy Policy.',
        style: theme.textTheme.bodyMedium?.copyWith(
          color: colorScheme.onSurface,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Widget _buildContactUs(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: appCardBorder(colorScheme),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: colorScheme.primary.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(AppRadius.sm),
                ),
                child: Icon(
                  Icons.mail_outline_rounded,
                  color: colorScheme.primary,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Text(
                'Contact Us',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _buildContactRow(
            context,
            label: 'App',
            value: appName,
            onTap: () => _launchUrl(_playStoreLink),
          ),
          const SizedBox(height: 12),
          _buildContactRow(
            context,
            label: 'Developer',
            value: 'Sajedur0',
            onTap: () => _launchUrl(_devPlayStoreLink),
          ),
          const SizedBox(height: 12),
          _buildContactRow(
            context,
            label: 'Email',
            value: 'Sajedurzero@gmail.com',
            onTap: () => _launchUrl('mailto:Sajedurzero@gmail.com'),
          ),
        ],
      ),
    );
  }

  Widget _buildContactRow(
    BuildContext context, {
    required String label,
    required String value,
    required VoidCallback onTap,
  }) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Row(
      children: [
        Text(
          '$label:',
          style: theme.textTheme.bodyMedium?.copyWith(
            color: colorScheme.onSurfaceVariant,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(width: 8),
        GestureDetector(
          onTap: onTap,
          child: Text(
            value,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: colorScheme.primary,
              fontWeight: FontWeight.w500,
              decoration: TextDecoration.underline,
              decorationColor: colorScheme.primary,
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _launchUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }
}
